package com.landicorp.seconddisplay.views;

import java.util.ArrayList;

import com.landicorp.player.IjkPlayView;
import com.landicorp.seconddisplay.R;

import android.annotation.SuppressLint;
import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;

public class AdDisplay extends Presentation {
	/**
	 * 辅屏幕广告类
	 */

	private String TAG = this.getClass().getSimpleName();
	private IjkPlayView mPlayView;
	private Context mContext;

	public AdDisplay(Context outerContext, Display display) {
		super(outerContext, display);
		mContext = outerContext;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.i("chengzhw", TAG + "-->oncreate");
		mPlayView = (IjkPlayView) findViewById(R.id.playview);
	}

	@SuppressLint("NewApi")
	@Override
	public void onDisplayRemoved() {
		// TODO Auto-generated method stub
		super.onDisplayRemoved();
		Log.i("chengzhw", TAG + "-->onDisplayRemoved");

	}

	@SuppressLint("NewApi")
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.i("chengzhw", TAG + "-->onStop");
		mPlayView.stopPlay();
	}

	@SuppressLint("NewApi")
	@Override
	public void onDisplayChanged() {
		// TODO Auto-generated method stub
		super.onDisplayChanged();
		Log.i("chengzhw", TAG + "-->onDisplayChanged");
	}

	@SuppressLint("NewApi")
	@Override
	public void onDetachedFromWindow() {
		// TODO Auto-generated method stub
		super.onDetachedFromWindow();
		Log.i("chengzhw", TAG + "-->onDetachedFromWindow");
	}

	public void startAd() {
		// TODO Auto-generated method stub
		Log.i("chengzhw", TAG + "-->startad");
		if (mPlayView != null) {
			mPlayView.startPlay();
		}
	}

	public void stopAd() {
		// TODO Auto-generated method stub
		if (mPlayView != null) {
			mPlayView.stopPlay();
		}
	}

	public void setAdList(ArrayList<String> ads) {
		// 设置图片轮播间隔时间
		mPlayView.setImageShowTime(10);
		mPlayView.setMediaItemsList(ads);

	}

	public void clearAdList() {
		mPlayView.stopPlay();
		mPlayView.setMediaItemsList(null);
		mPlayView.setCurrentIndex(0);
	}

}
