package com.landicorp.player;

import java.util.ArrayList;

import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

@SuppressLint("NewApi")
public class PlayView3 extends RelativeLayout implements Callback  {
	private Context mContext;
	private ImageView mImageView;
	private AspectRatioFrameLayout mAspectRatioFrameLayout;
	private SurfaceView mSurfaceView;
	private SurfaceHolder mVideoHolder = null;
	private boolean mPlayerNeedsPrepare;
	private int mCurrentPosition = -1;
	private ArrayList<String> mMediaItemsList = null;// 资源列表
	private int mDefaultImage = -1;
	private Handler mHandler;
	private int mImageShowTime = 10;

	public PlayView3(Context context) {
		super(context);
		mContext = context;
	}

	public PlayView3(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	@SuppressLint("NewApi")
	public PlayView3(Context context, AttributeSet attrs, int defStyle) {
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
			startNext();
		}
	};

	private void startNext() {
		if (mMediaItemsList == null || mMediaItemsList.isEmpty() || mMediaItemsList.size() == 0) {
			return;
		}
		if (mCurrentPosition < 0) {
			mCurrentPosition = 0;
		} else if (mCurrentPosition + 1 >= mMediaItemsList.size()) {
			mCurrentPosition = 0;
		} else {
			mCurrentPosition++;
		}
		play(mMediaItemsList.get(mCurrentPosition));
	}

	@SuppressLint({ "ResourceAsColor", "DefaultLocale" })
	private void play(String path) {

		Log.w("troy", "play ad path:" + path);

		if (path.trim().toLowerCase().endsWith(".jpg")) {
			showImage(path);
		} else if (path.trim().toLowerCase().endsWith(".mp4")) {
			mSurfaceView.setVisibility(View.VISIBLE);
			setViewParams(path);
			showVideo();
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

	private void showVideo() {

		 
//		NativePlayer.getInstance().startPlay(mMediaItemsList.get(mCurrentPosition),
//				mVideoHolder.getSurface());
	}

	private void releasePlayer() {
		 
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mVideoHolder = holder;
		NativePlayer.getInstance().startPlay(mMediaItemsList.get(mCurrentPosition),
				mVideoHolder.getSurface());
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		 
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
