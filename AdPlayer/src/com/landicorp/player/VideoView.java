package com.landicorp.player;

import java.io.File;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.util.Util;
import com.landicorp.player.exo.ExtractorRendererBuilder;
import com.landicorp.player.exo.VideoPlayer;
import com.landicorp.player.exo.VideoPlayer.Listener;
import com.landicorp.player.exo.VideoPlayer.RendererBuilder;

@SuppressLint("NewApi")
public class VideoView extends RelativeLayout implements Callback, Listener {
	private Context mContext;
	private VideoPlayer mVideoPlayer;
	private AspectRatioFrameLayout mAspectRatioFrameLayout;
	private SurfaceView mSurfaceView;

	private boolean mPlayerNeedsPrepare;

	private VideoCallBack mVideoCallBack = null;

	public VideoView(Context context) {
		super(context);
		mContext = context;
		viewInit();
	}

	public VideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		viewInit();
	}

	@SuppressLint("NewApi")
	public VideoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		viewInit();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		// viewInit();
	}

	private void viewInit() {

		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		mAspectRatioFrameLayout = new AspectRatioFrameLayout(mContext);
		mSurfaceView = new SurfaceView(mContext);
		mAspectRatioFrameLayout.setLayoutParams(layoutParams);
		mSurfaceView.setLayoutParams(layoutParams);
		mAspectRatioFrameLayout.addView(mSurfaceView);

		this.addView(mAspectRatioFrameLayout);
		mSurfaceView.getHolder().addCallback(this);

	}

	@SuppressLint({ "ResourceAsColor", "DefaultLocale" })
	public void play(String path) {

		if (path.trim().toLowerCase().endsWith(".mp4")) {
			mSurfaceView.setVisibility(View.VISIBLE);
			setViewParams(path);
			showVideo(true, path);
		}

	}

	private void showVideo(boolean playWhenReady, String path) {

		if (mVideoPlayer == null) {

			String userAgent = Util.getUserAgent(mContext, "ExoPlayerDemo");
			File file = new File(path);
			Uri mUri = Uri.fromFile(file);
			RendererBuilder rendererBuilder = new ExtractorRendererBuilder(mContext, userAgent, mUri);

			mVideoPlayer = new VideoPlayer(rendererBuilder);
			mVideoPlayer.addListener(this);
			mVideoPlayer.seekTo(0);
			mPlayerNeedsPrepare = true;

		}
		if (mPlayerNeedsPrepare) {
			mVideoPlayer.seekTo(0);
			mVideoPlayer.prepare();
			mPlayerNeedsPrepare = false;
		}
		mVideoPlayer.setSurface(mSurfaceView.getHolder().getSurface());
		mVideoPlayer.setPlayWhenReady(playWhenReady);
	}

	public void releasePlayer() {
		if (mVideoPlayer != null) {
			mVideoPlayer.release();
			mVideoPlayer = null;
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (mVideoPlayer != null) {
			mVideoPlayer.setSurface(holder.getSurface());
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mVideoPlayer != null) {
			mVideoPlayer.blockingClearSurface();
		}
	}

	private void setViewParams(String path) {

		int parentWidth = this.getWidth();
		int parentHeight = this.getHeight();
		int originalHeight = 0;
		int originalWidth = 0;
		int finalWidth = parentWidth;
		int finalHeight = parentHeight;
		LayoutParams layoutParams = (LayoutParams) mAspectRatioFrameLayout.getLayoutParams();
		try {

			MediaMetadataRetriever mmr = new MediaMetadataRetriever();
			mmr.setDataSource(path);
			String heightString = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
			String weightString = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
			originalHeight = Integer.parseInt(heightString);// 视频分辨率
			originalWidth = Integer.parseInt(weightString);
			mmr.release();

			// 需要缩放
			if (originalHeight >= parentHeight && originalWidth >= parentWidth) {
				float hRatio = (float) originalHeight / (float) parentHeight;
				float wRatio = (float) originalWidth / (float) parentWidth;
				float ratio = Math.max(wRatio, hRatio);// 获取最大缩放比
				finalHeight = (int) Math.abs(originalHeight / ratio);
				finalWidth = (int) Math.abs(originalWidth / ratio);
			}
			// 需要拉伸
			else if (originalHeight <= parentHeight && originalWidth <= parentWidth) {

				float hRatio = (float) originalHeight / (float) parentHeight;
				float wRatio = (float) originalWidth / (float) parentWidth;

				float ratio = Math.max(wRatio, hRatio);// 获取最大缩放比

				finalHeight = (int) Math.abs(originalHeight / ratio);
				finalWidth = (int) Math.abs(originalWidth / ratio);
				/*
				 * finalHeight = originalHeight; finalWidth = originalWidth;
				 */
			} else if (originalHeight <= parentHeight && originalWidth >= parentWidth) {
				float wRatio = (float) originalWidth / (float) parentWidth;

				finalHeight = (int) Math.abs(originalHeight / wRatio);
				finalWidth = (int) Math.abs(originalWidth / wRatio);
			} else if (originalHeight >= parentHeight && originalWidth <= parentWidth) {
				float hRatio = (float) originalHeight / (float) parentHeight;

				finalHeight = (int) Math.abs(originalHeight / hRatio);
				finalWidth = (int) Math.abs(originalWidth / hRatio);
			}

			// marginLeft = Math.abs(parentWidth - finalWidth) / 2;
			// marginRight = Math.abs(parentWidth - finalWidth) / 2;
			// marginTop = Math.abs(parentHeight - finalHeight) / 2;
			// marginButtom = Math.abs(parentHeight - finalHeight) / 2;

			layoutParams.height = finalHeight;
			layoutParams.width = finalWidth;

			// layoutParams.leftMargin = marginLeft;
			// layoutParams.rightMargin = marginRight;
			// layoutParams.topMargin = marginTop;
			// layoutParams.bottomMargin = marginButtom;
			layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

			mSurfaceView.getHolder().setFixedSize(finalWidth, finalHeight);
			mAspectRatioFrameLayout.setLayoutParams(layoutParams);
		} catch (Exception e) {

		}
	}

	@Override
	public void onStateChanged(boolean playWhenReady, int playbackState) {

		switch (playbackState) {
		case ExoPlayer.STATE_BUFFERING:
			break;
		case ExoPlayer.STATE_ENDED:
			releasePlayer();
			if (mVideoCallBack != null)
				mVideoCallBack.videoEnd();
			break;
		case ExoPlayer.STATE_IDLE:
			break;
		case ExoPlayer.STATE_PREPARING:
			break;
		case ExoPlayer.STATE_READY:
			if (mVideoCallBack != null)
				mVideoCallBack.videoReady();
			break;
		default:
			break;
		}
	}

	@Override
	public void onError(Exception e) {
		Log.e("troy", "exoplayer Exception :" + e.getMessage());
		if (mVideoCallBack != null)
			mVideoCallBack.videoEnd();
	}

	@Override
	public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthAspectRatio) {
		// Log.w("troy", "onVideoSizeChanged start");
		// // MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		// //
		// // String path = mMediaItemsList.get(mCurrentPosition);
		// // mmr.setDataSource(path);
		// mMediaMetadataRetriever = new MediaMetadataRetriever();
		// String path = mMediaItemsList.get(mCurrentPosition);
		// mMediaMetadataRetriever.setDataSource(path);
		// if (mMediaMetadataRetriever == null)
		// return;
		// String heightString =
		// mMediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
		// String weightString =
		// mMediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
		// int originalHeight = Integer.parseInt(heightString);// 视频分辨率
		// int originalWidth = Integer.parseInt(weightString);
		// mMediaMetadataRetriever.release();
		// mMediaMetadataRetriever = null;
		// float Ratio = 1;
		// if (height != 0) {
		// Ratio = (originalWidth * pixelWidthAspectRatio) / originalHeight;
		// }
		//
		// mAspectRatioFrameLayout.setAspectRatio(Ratio);
		// Log.w("troy", "onVideoSizeChanged end");
	}

	public void setVideoCallBack(VideoCallBack mVideoCallBack) {
		this.mVideoCallBack = mVideoCallBack;
	}

	public interface VideoCallBack {
		public void videoReady();

		public void videoEnd();
	}

}
