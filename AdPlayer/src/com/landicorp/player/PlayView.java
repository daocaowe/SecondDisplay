package com.landicorp.player;

import java.io.File;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.util.Util;
import com.landicorp.player.exo.ExtractorRendererBuilder;
import com.landicorp.player.exo.VideoPlayer;
import com.landicorp.player.exo.VideoPlayer.Listener;
import com.landicorp.player.exo.VideoPlayer.RendererBuilder;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

@SuppressLint("NewApi")
public class PlayView extends RelativeLayout implements Callback, Listener {
	private Context mContext;
	private ImageView mImageView;
	private VideoPlayer mVideoPlayer;
	private AspectRatioFrameLayout mAspectRatioFrameLayout;
	private SurfaceView mSurfaceView;

	private boolean mPlayerNeedsPrepare;
	private int mCurrentPosition = 0;
	private ArrayList<String> mMediaItemsList = null;// 资源列表
	private int mDefaultImage = -1;
	private Handler mHandler;
	private int mImageShowTime = 10;

	public PlayView(Context context) {
		super(context);
		mContext = context;
	}

	public PlayView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	@SuppressLint("NewApi")
	public PlayView(Context context, AttributeSet attrs, int defStyle) {
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
		stopPlay();
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasWindowFocus);
		if (!hasWindowFocus) {
			stopPlay();
		}

	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		// TODO Auto-generated method stub
		super.onWindowVisibilityChanged(visibility);
		if (visibility == View.INVISIBLE || visibility == View.GONE) {
			stopPlay();
		}

	}

	private void viewInit() {

		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		mImageView = new ImageView(mContext);
		mAspectRatioFrameLayout = new AspectRatioFrameLayout(mContext);
		mSurfaceView = new SurfaceView(mContext);
		mImageView.setLayoutParams(layoutParams);
		mAspectRatioFrameLayout.setLayoutParams(layoutParams);
		mSurfaceView.setLayoutParams(layoutParams);
		mAspectRatioFrameLayout.addView(mSurfaceView);

		this.addView(mAspectRatioFrameLayout);
		this.addView(mImageView);
		mSurfaceView.getHolder().addCallback(this);

		ImageUtil.getInstance().create(mContext.getApplicationContext());
	}

	public int getCurrentIndex() {
		return mCurrentPosition;
	}

	public void setCurrentIndex(int mCurrentPosition) {
		this.mCurrentPosition = mCurrentPosition;
	}

	/**
	 * 启动播放
	 * 
	 * @param mMediaItemsList
	 *            要播放的视频与图片路径
	 */
	public void startPlay() {
		if (mMediaItemsList == null || mMediaItemsList.size() == 0)
			return;
		startNext();
	}

	/**
	 * 启动播放
	 * 
	 * @param mMediaItemsList
	 *            要播放的视频与图片路径
	 */
	public void startPlay(ArrayList<String> mMediaItemsList) {
		this.mMediaItemsList = mMediaItemsList;
		startNext();
	}

	public void stopPlay() {
		Log.w("troy", "---------------stopPlay------------");

		if (mSurfaceView != null) {
			releasePlayer();
		}
		if (mHandler != null && mPlayNextRunnable != null)
			mHandler.removeCallbacks(mPlayNextRunnable);
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
			startNext();
		}
	};

	private void startNext() {
		if (mMediaItemsList == null || mMediaItemsList.isEmpty() || mMediaItemsList.size() == 0) {
			return;
		}
		play(mMediaItemsList.get(mCurrentPosition));
	}

	int mSeekWhenPrepared = 0;

	public void seekTo(int msec) {
		mSeekWhenPrepared = msec;
	}

	public int getCurrentPosition() {
		return (int) mVideoPlayer.getCurrentPosition();
	}

	@SuppressLint({ "ResourceAsColor", "DefaultLocale" })
	private void play(String path) {

		Log.w("troy", "play ad path:" + path);

		if (path.trim().toLowerCase().endsWith(".jpg")) {
			showImage(path);
		} else if (path.trim().toLowerCase().endsWith(".mp4")) {
			mSurfaceView.setVisibility(View.VISIBLE);
			setViewParams(path);
			showVideo(true);
		}

	}

	private void showImage(String path) {
		if (path.length() == 0) {
			return;
		}
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
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
				mSurfaceView.setVisibility(View.INVISIBLE);
				if (mDefaultImage > 0)
					mImageView.setImageDrawable(mContext.getResources().getDrawable(mDefaultImage));
			}

			@Override
			public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
				mImageView.setVisibility(View.VISIBLE);
				mSurfaceView.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onLoadingCancelled(String arg0, View arg1) {
				mImageView.setVisibility(View.VISIBLE);
				mSurfaceView.setVisibility(View.INVISIBLE);
				if (mDefaultImage > 0)
					mImageView.setImageDrawable(mContext.getResources().getDrawable(mDefaultImage));
			}
		});
		if (mMediaItemsList.size() > 1) {
			mHandler.removeCallbacks(mPlayNextRunnable);
			mHandler.postDelayed(mPlayNextRunnable, mImageShowTime * 1000);
		}
	}

	private void showVideo(boolean playWhenReady) {

		if (mVideoPlayer == null) {

			String path = mMediaItemsList.get(mCurrentPosition);
			String userAgent = Util.getUserAgent(mContext, "ExoPlayerDemo");
			File file = new File(path);
			Uri mUri = Uri.fromFile(file);
			RendererBuilder rendererBuilder = new ExtractorRendererBuilder(mContext, userAgent, mUri);

			mVideoPlayer = new VideoPlayer(rendererBuilder);
			mVideoPlayer.addListener(this);
			if (mSeekWhenPrepared > 0) {

				mVideoPlayer.seekTo(mSeekWhenPrepared);
				mSeekWhenPrepared = 0;
			} else
				mVideoPlayer.seekTo(0);
			mPlayerNeedsPrepare = true;

		}
		if (mPlayerNeedsPrepare) {
			if (mSeekWhenPrepared > 0) {

				mVideoPlayer.seekTo(mSeekWhenPrepared);
				mSeekWhenPrepared = 0;
			} else
				mVideoPlayer.seekTo(0);
			mVideoPlayer.prepare();
			mPlayerNeedsPrepare = false;
		}
		mVideoPlayer.setSurface(mSurfaceView.getHolder().getSurface());
		mVideoPlayer.setPlayWhenReady(playWhenReady);
	}

	private void releasePlayer() {
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

			startNext();
			break;
		case ExoPlayer.STATE_IDLE:
			break;
		case ExoPlayer.STATE_PREPARING:
			break;
		case ExoPlayer.STATE_READY:
			mImageView.setImageResource(android.R.color.transparent);
			mImageView.setVisibility(View.INVISIBLE);
			break;
		default:
			break;
		}
	}

	@Override
	public void onError(Exception e) {
		Log.e("troy", "exoplayer Exception :" + e.getMessage());
		mHandler.post(mPlayNextRunnable);

	}

	@Override
	public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthAspectRatio) {

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

}
