package com.landicorp.player;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

public class NativePlayer {
	private static String TAG = NativePlayer.class.getSimpleName();
	private String mFileString = null;
	private nativeVideoPlayThread mVideoPlayThread = null;
	private nativeAudioPlayThread mAudioPlayThread = null;
	private MediaCodec mVideoDecoder = null;
	private MediaCodec mAudioDecoder = null;

	private AudioTrack mAudioTrack;
	private int mAudioStreamType;
	private boolean doStopVideo = false;
	private boolean doStopAudio = false;
	public FinishCBInterface mFinishCBInterface;
	private static NativePlayer mNativePlayer;
	private static Lock mLock = new ReentrantLock();

	private NativePlayer() {
		mAudioStreamType = AudioManager.STREAM_ALARM;
	}

	public interface FinishCBInterface {
		public void finishCB();

	}

	public static NativePlayer getInstance() {
		mNativePlayer = new NativePlayer();
		return mNativePlayer;
	}

	public void setCallfuc(FinishCBInterface FCB) {
		this.mFinishCBInterface = FCB;
	}

	public void callFinishCB() {
		if (this.mFinishCBInterface != null)
			this.mFinishCBInterface.finishCB();
	}

	public void startPlay(String filename, Surface surface) {
		Log.d(TAG, "file name: " + filename);
		mLock.lock();
		mAudioPlayThread = new nativeAudioPlayThread(filename, surface);
		mAudioPlayThread.start();
		doStopAudio = false;
		mVideoPlayThread = new nativeVideoPlayThread(filename, surface);
		mVideoPlayThread.start();
		doStopVideo = false;
		mLock.unlock();
	}

	@SuppressLint("NewApi")
	public void stopPlay() {
		mLock.lock();
		doStopVideo = true;
		doStopAudio = true;
		mLock.unlock();
	}

	private class nativeVideoPlayThread extends Thread {
		private MediaExtractor extractor;
		private Surface surface;

		public nativeVideoPlayThread(String filename, Surface surface) {
			this.surface = surface;
			mFileString = filename;
		}

		@SuppressLint("NewApi")
		@Override
		public void run() {

			mVideoDecoder = null;
			extractor = new MediaExtractor();
			try {
				extractor.setDataSource(mFileString);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			mLock.lock();//
			if (doStopVideo == false) {
				for (int i = 0; i < extractor.getTrackCount(); i++) {
					MediaFormat format = extractor.getTrackFormat(i);
					String mime = format.getString(MediaFormat.KEY_MIME);

					if (mime.startsWith("video/")) {
						extractor.selectTrack(i);
						try {
							mVideoDecoder = MediaCodec.createDecoderByType(mime);
							mVideoDecoder.configure(format, surface, null, 0);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						break;
					} else {
						Log.e(TAG, "surface null");
					}
				}
			} else {
				Log.e(TAG, "video is stoped");
			}
			mLock.unlock();

			if (mVideoDecoder == null) {
				extractor.release();
				return;
			}

			mVideoDecoder.start();
			ByteBuffer[] inputBuffers = mVideoDecoder.getInputBuffers();
			ByteBuffer[] outputBuffers = mVideoDecoder.getOutputBuffers();
			BufferInfo info = new BufferInfo();
			boolean isEOS = false;

			long startMs = System.currentTimeMillis();

			while (!Thread.interrupted() && !doStopVideo) {

				if (!isEOS) {
					int inIndex = mVideoDecoder.dequeueInputBuffer(10000);
					if (inIndex >= 0) {
						ByteBuffer buffer = inputBuffers[inIndex];
						int sampleSize = extractor.readSampleData(buffer, 0);
						if (sampleSize < 0) {
							mVideoDecoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
							isEOS = true;
						} else {
							mVideoDecoder.queueInputBuffer(inIndex, 0, sampleSize, extractor.getSampleTime(), 0);
							extractor.advance();
						}
					}
				}

				int outIndex;
				mLock.lock();
				if (doStopVideo == false) {
					outIndex = mVideoDecoder.dequeueOutputBuffer(info, 10000);
				} else {
					mLock.unlock();
					break;
				}
				mLock.unlock();

				switch (outIndex) {
				case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
					// Log.i(TAG, "----------- INFO_OUTPUT_BUFFERS_CHANGED");
					outputBuffers = mVideoDecoder.getOutputBuffers();
					break;
				case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
					// Log.i(TAG, "------------ New format " +
					// mVideoDecoder.getOutputFormat());
					break;
				case MediaCodec.INFO_TRY_AGAIN_LATER:
					// Log.i(TAG,
					// "----------- dequeueOutputBuffer timed outm, try again
					// later!");
					break;
				default:
					// Log.e(TAG,
					// "VIDEO decoder release buffer, info.presentationTimeUs" +
					// info.presentationTimeUs);
					ByteBuffer buffer = outputBuffers[outIndex];
					// We use a very simple clock to keep the video FPS, or the
					// video playback will be too fast
					while (info.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
						try {
							sleep(1);
						} catch (InterruptedException e) {
							e.printStackTrace();
							break;
						}
					}
					mVideoDecoder.releaseOutputBuffer(outIndex, true);
					break;
				}
				// All decoded frames have been rendered, we can stop playing
				// now
				if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
					break;
				}
			}
			// Log.d(TAG, "video play thread stop!!!!!");
			mVideoDecoder.stop();
			mVideoDecoder.release();
			extractor.release();
		}
	}

	private class nativeAudioPlayThread extends Thread {
		private MediaExtractor extractor;
		private Surface surface;

		public nativeAudioPlayThread(String filename, Surface surface) {
			this.surface = surface;
			mFileString = filename;
		}

		@SuppressLint("NewApi")
		@Override
		public void run() {
			mAudioDecoder = null;
			extractor = new MediaExtractor();
			try {
				extractor.setDataSource(mFileString);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			mLock.lock();
			if (doStopAudio == false) {
				for (int i = 0; i < extractor.getTrackCount(); i++) {
					MediaFormat format = extractor.getTrackFormat(i);
					String mime = format.getString(MediaFormat.KEY_MIME);
					Log.e(TAG, "mime: "+mime);
					if (mime.startsWith("audio/")) {
						Log.e(TAG, "create AUDIO decoder by type");
						try {
							mAudioDecoder = MediaCodec.createDecoderByType(mime);
							mAudioDecoder.configure(format, null, null, 0);
							// create our AudioTrack instance
							int sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
							mAudioTrack = new AudioTrack(mAudioStreamType, sampleRate, AudioFormat.CHANNEL_OUT_STEREO,
									AudioFormat.ENCODING_PCM_16BIT, AudioTrack.getMinBufferSize(sampleRate,
											AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT),
									AudioTrack.MODE_STREAM);
							extractor.selectTrack(i);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						break;
					}
				}
			} else {
				Log.e(TAG, "audio is stoped");
			}
			mLock.unlock();

			if (mAudioDecoder == null) {
				Log.e(TAG, "mAudioDecoder is null!!!!!!!!!!");
				extractor.release();
				nativePlayerFinish();
				return;
			}
			final long kTimeOutUs = 10000;

			// start decoding
			mAudioDecoder.start();
			ByteBuffer[] inputBuffers = mAudioDecoder.getInputBuffers();
			ByteBuffer[] outputBuffers = mAudioDecoder.getOutputBuffers();
			BufferInfo info = new BufferInfo();
			boolean isEOS = false;
			long startMs = System.currentTimeMillis();

			// start playing
			mAudioTrack.play();

			while (!Thread.interrupted() && !doStopAudio) {
				if (!isEOS) {
					int inIndex = mAudioDecoder.dequeueInputBuffer(kTimeOutUs);
					if (inIndex >= 0) {
						ByteBuffer buffer = inputBuffers[inIndex];
						int sampleSize = extractor.readSampleData(buffer, 0);
						if (sampleSize < 0) {
							mAudioDecoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
							isEOS = true;
						} else {
							mAudioDecoder.queueInputBuffer(inIndex, 0, sampleSize, extractor.getSampleTime(), 0);
							extractor.advance();
						}
					}
				}
				int outIndex = mAudioDecoder.dequeueOutputBuffer(info, kTimeOutUs);
				switch (outIndex) {
				case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
					// Log.i(TAG, "----------- INFO_OUTPUT_BUFFERS_CHANGED");
					outputBuffers = mAudioDecoder.getOutputBuffers();
					break;
				case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
					// Log.i(TAG, "------------ New format " +
					// mAudioDecoder.getOutputFormat());
					break;
				case MediaCodec.INFO_TRY_AGAIN_LATER:
					// Log.i(TAG,
					// "----------- dequeueOutputBuffer timed outm, try again
					// later!");
					break;
				default:
					ByteBuffer buf = outputBuffers[outIndex];
					final byte[] chunk = new byte[info.size];
					buf.get(chunk);
					buf.clear();
					if (chunk.length > 0) {
						mAudioTrack.write(chunk, 0, chunk.length);
					}
					mAudioDecoder.releaseOutputBuffer(outIndex, true);
					break;
				}

				// All decoded frames have been rendered, we can stop playing
				// now
				if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
					break;
				}
			}
			// Log.d(TAG, "audio play thread stop!!!!!");
			mAudioTrack.stop();
			mAudioTrack.release();
			mAudioDecoder.stop();
			mAudioDecoder.release();
			extractor.release();
			nativePlayerFinish();
		}
	}

	private void nativePlayerFinish() {
		try {
			mVideoPlayThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d(TAG, "native player stoped!!!!!");
		if (!doStopAudio) {
			callFinishCB();
		}
	}

}
