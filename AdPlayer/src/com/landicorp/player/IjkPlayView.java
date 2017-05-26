package com.landicorp.player;

import java.util.ArrayList;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnCompletionListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnInfoListener;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.landicorp.player.ijkplayer.IjkVideoView;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

@SuppressLint("NewApi")
public class IjkPlayView extends RelativeLayout implements OnCompletionListener, OnInfoListener {
	String TAG = "troy";
	private Context mContext;
	private ImageView mImageView;
	private IjkVideoView mIjkVideoView;
	private int mCurrentPosition = 0;
	private ArrayList<String> mMediaItemsList = null;// 璧勬簮鍒楄〃
	private int mDefaultImage = -1;
	private Handler mHandler;
	private int mImageShowTime = 10;
	private int mSeekWhenPrepared = 0;;
	private boolean isStop = false;

	public IjkPlayView(Context context) {
		super(context);
		mContext = context;
	}

	public IjkPlayView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	@SuppressLint("NewApi")
	public IjkPlayView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mHandler = new Handler();
		viewInit();
	}

	@Override
	protected void onDetachedFromWindow() {
		// TODO Auto-generated method stub
		super.onDetachedFromWindow();
		Log.w(TAG, "-----------onDetachedFromWindow AD stopPlay------------");
		stopPlay();
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasWindowFocus);
		if (!hasWindowFocus) {
			Log.w(TAG, "-----------onon WindowFocusChanged AD stopPlay------------");
//			stopPlay();
		}

	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		// TODO Auto-generated method stub
		super.onWindowVisibilityChanged(visibility);
		if (visibility == View.INVISIBLE || visibility == View.GONE) {
			Log.w(TAG, "-----------onWindowVisibilityChanged AD stopPlay------------");
			stopPlay();
		}

	}

	@SuppressLint("ResourceAsColor")
	private void viewInit() {
		IjkPlayView.this.removeAllViews();
		// this.setBackgroundColor(android.R.color.white);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
//		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		mImageView = new ImageView(mContext);
		mIjkVideoView = new IjkVideoView(mContext);
		mImageView.setLayoutParams(layoutParams);
		mIjkVideoView.setLayoutParams(layoutParams);
		// mImageView.setImageResource(android.R.color.white);
		mIjkVideoView.setVisibility(View.INVISIBLE);
		IjkPlayView.this.addView(mIjkVideoView);
		IjkPlayView.this.addView(mImageView);

		mIjkVideoView.setOnCompletionListener(this);
		mIjkVideoView.setOnInfoListener(this);
		ImageUtil.getInstance().create(mContext.getApplicationContext());
	}

	/**
	 * 鍚姩鎾斁
	 * 
	 * @param mMediaItemsList
	 *            瑕佹挱鏀剧殑瑙嗛涓庡浘鐗囪矾寰�
	 */
	public void startPlay() {
		if (mMediaItemsList == null || mMediaItemsList.size() == 0)
			return;
		isStop = false;
		Log.w(TAG, "---#####-----AD startPlay------####---");
		IjkPlayView.this.setVisibility(View.VISIBLE);
		playAd();
	}

	/**
	 * 鍚姩鎾斁
	 * 
	 * @param mMediaItemsList
	 *            瑕佹挱鏀剧殑瑙嗛涓庡浘鐗囪矾寰�
	 */
	public void startPlay(ArrayList<String> mMediaItemsList) {
		this.mMediaItemsList = mMediaItemsList;
		playAd();
	}

	public void stopPlay() {
		Log.w(TAG, "-----------AD stopPlay------------" + isStop);
		if (isStop)
			return;
		isStop = true;
		if (mHandler != null && mPlayNextRunnable != null)
			mHandler.removeCallbacks(mPlayNextRunnable);
		if (mIjkVideoView != null) {
			// releasePlayer();
			mIjkVideoView.stopPlayback();
		}
		IjkPlayView.this.setVisibility(View.GONE);

	}

	private Runnable mPlayNextRunnable = new Runnable() {
		@Override
		public void run() {
			if (mCurrentPosition < 0) {
				mCurrentPosition = 0;
			} else if (mCurrentPosition + 1 >= mMediaItemsList.size()) {
				mCurrentPosition = 0;
			} else {
				mCurrentPosition++;
			}
			playAd();
		}
	};

	private void playAd() {
		if (mMediaItemsList == null || mMediaItemsList.isEmpty() || mMediaItemsList.size() == 0) {
			return;
		}
		if(mCurrentPosition>mMediaItemsList.size())
		{
			return;
		}
		play(mMediaItemsList.get(mCurrentPosition));
	}

	public int getCurrentIndex() {
		return mCurrentPosition;
	}

	public void setCurrentIndex(int mCurrentPosition) {
		this.mCurrentPosition = mCurrentPosition;
	}

	public void seekTo(int msec) {
		mSeekWhenPrepared = msec;
	}

	@SuppressLint({ "ResourceAsColor", "DefaultLocale" })
	private void play(String path) {

		Log.w("troy", "play ad path:" + path);

		if (path.trim().toLowerCase().endsWith(".jpg")||path.trim().toLowerCase().endsWith(".png")) {
			showImage(path);
		} else if (path.trim().toLowerCase().endsWith(".mp4")) {
			showVideo(path);
		}

	}

	private void showImage(String path) {
		if (path.length() == 0) {
			return;
		}
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
//		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		mImageView.setLayoutParams(layoutParams);

		path = "file://" + path;
		ImageUtil.getInstance().clearCache();
		ImageUtil.getInstance().showImage(mImageView, path, new ImageLoadingListener() {

			@Override
			public void onLoadingStarted(String arg0, View arg1) {

			}

			@Override
			public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
				mImageView.setVisibility(View.VISIBLE);
				mIjkVideoView.setVisibility(View.INVISIBLE);
				if (mDefaultImage > 0)
					mImageView.setImageDrawable(mContext.getResources().getDrawable(mDefaultImage));
			}

			@Override
			public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
				mImageView.setVisibility(View.VISIBLE);
				mIjkVideoView.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onLoadingCancelled(String arg0, View arg1) {
				mImageView.setVisibility(View.VISIBLE);
				mIjkVideoView.setVisibility(View.INVISIBLE);
				if (mDefaultImage > 0)
					mImageView.setImageDrawable(mContext.getResources().getDrawable(mDefaultImage));
			}
		});
		if (mMediaItemsList.size() > 1) {
			mHandler.removeCallbacks(mPlayNextRunnable);
			mHandler.postDelayed(mPlayNextRunnable, mImageShowTime * 1000);
		}
	}

	@Override
	public void onCompletion(IMediaPlayer arg0) {
		mHandler.post(mPlayNextRunnable);
	}

	private void showVideo(String path) {
		mIjkVideoView.setVisibility(View.INVISIBLE);
		mIjkVideoView.setVideoPath(path);
		mIjkVideoView.seekTo(mSeekWhenPrepared);
		mSeekWhenPrepared = 0;
		mIjkVideoView.start();

		Log.d(TAG, "IjkVideoView.start:");
	}

	@Override
	public boolean onInfo(IMediaPlayer arg0, int arg1, int extra) {
		switch (arg1) {
		case IMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
			Log.d(TAG, "MEDIA_INFO_VIDEO_TRACK_LAGGING:");
			break;
		case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
			Log.d(TAG, "MEDIA_INFO_VIDEO_RENDERING_START:");
			mImageView.setImageResource(android.R.color.transparent);
			mImageView.setVisibility(View.INVISIBLE);
			mIjkVideoView.setVisibility(View.VISIBLE);
			break;
		case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
			Log.d(TAG, "MEDIA_INFO_BUFFERING_START:");

			break;
		case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
			Log.d(TAG, "MEDIA_INFO_BUFFERING_END:");
			break;
		case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH:
			Log.d(TAG, "MEDIA_INFO_NETWORK_BANDWIDTH: " + extra);
			break;
		case IMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
			Log.d(TAG, "MEDIA_INFO_BAD_INTERLEAVING:");
			break;
		case IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
			Log.d(TAG, "MEDIA_INFO_NOT_SEEKABLE:");
			break;
		case IMediaPlayer.MEDIA_INFO_METADATA_UPDATE:
			Log.d(TAG, "MEDIA_INFO_METADATA_UPDATE:");
			break;
		case IMediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
			Log.d(TAG, "MEDIA_INFO_UNSUPPORTED_SUBTITLE:");
			break;
		case IMediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
			Log.d(TAG, "MEDIA_INFO_SUBTITLE_TIMED_OUT:");
			break;
		case IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED:

			break;
		case IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START:
			Log.d(TAG, "MEDIA_INFO_AUDIO_RENDERING_START:");
			break;
		}
		return false;
	}

	public void releasePlayer() {
		Log.w(TAG, "-----------AD releasePlayer------------");
		mIjkVideoView.stopPlayback();
		mIjkVideoView.release(true);
		IjkMediaPlayer.native_profileEnd();
	}

	public void setMediaItemsList(ArrayList<String> mMediaItemsList) {
		this.mMediaItemsList = mMediaItemsList;
	}

	public void setDefaultImage(int mDefaultImage) {
		this.mDefaultImage = mDefaultImage;
	}

	public void setImageShowTime(int mImageShowTime) {
		this.mImageShowTime = mImageShowTime;
	}

	public int getCurrentPosition() {
		return (int) mIjkVideoView.getCurrentPosition();
	}

	public void hiddenVideoView() {
		mIjkVideoView.hiddenRenderView();
	}

	public void showVideoView() {
		mIjkVideoView.showRenderView();
	}
}
