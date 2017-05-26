package com.landicorp.seconddisplay.services;

import java.util.ArrayList;

import com.landicorp.seconddisplay.R;
import com.landicorp.seconddisplay.views.AdDisplay;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRouter;
import android.media.MediaRouter.RouteInfo;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

@SuppressLint("NewApi")
public class PressentationService extends Service {
	/**
	 * 辅屏幕显示，常驻后端服务方式
	 */
	private String TAG = "PressentationService-->";
	private AdDisplay adDisplay = null;
	private MediaRouter mMediaRouter;
	Receiver receiver;
	boolean isExit = false;
	ArrayList<String> adList = new ArrayList<String>();

	@Override
	public void onCreate() {
		super.onCreate();
		receiver = new Receiver();
		mMediaRouter = (MediaRouter) getSystemService(Context.MEDIA_ROUTER_SERVICE);
		mMediaRouter.addCallback(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, mMediaRouterCallback);
	}

	@Override
	public void onDestroy() {
		if (null != adDisplay) {
			adDisplay.stopAd();
		}
		unregisterReceiver(receiver);
		Log.i("chengzhw", "###############close screen service##############");
		mMediaRouter.removeCallback(mMediaRouterCallback);
		dismissADPresentation();
		stopForeground(true);
		super.onDestroy();

	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	int startId;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		this.startId = startId;
		IntentFilter filter = new IntentFilter();
		// 开始播放广告
		filter.addAction("com.landicorp.SECOND_DISPLAY_START_AD");
		// 停止播放广告
		filter.addAction("com.landicorp.SECOND_DISPLAY_STOP_AD");
		// 清除广告列表
		filter.addAction("com.landicorp.SECOND_DISPLAY_CLEAR_AD");
		// 添加广告文件
		filter.addAction("com.landicorp.SECOND_DISPLAY_ADD_AD");
		filter.setPriority(1000);
		registerReceiver(receiver, filter);
		return super.onStartCommand(intent, START_NOT_STICKY, startId);
	}

	/**
	 * 创建辅屏幕广告，常驻在系统后台服务。
	 * 
	 * @param message
	 */
	private void showADPresentation() {
		if (null == adList || adList.size() <= 0) {
			Toast.makeText(PressentationService.this, "请选择播放文件", 1000).show();
			return;
		}
		if (null == adDisplay) {
			MediaRouter.RouteInfo route = mMediaRouter.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
			Display presentationDisplay = route != null ? route.getPresentationDisplay() : null;
			if (presentationDisplay == null) {
				return;
			}
			adDisplay = new AdDisplay(getApplicationContext(), presentationDisplay);
			try {
				adDisplay.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
				adDisplay.setContentView(R.layout.pressentation_ad);

			} catch (WindowManager.InvalidDisplayException ex) {
				adDisplay = null;
			}
		}

		if (adList == null || adList.size() <= 0) {
			return;
		}
		adDisplay.show();

		adDisplay.setAdList(adList);
		adDisplay.startAd();
	}

	/**
	 * 关闭广告
	 */
	private void dismissADPresentation() {
		if (adDisplay != null && adDisplay.isShowing()) {
			Log.i("chengzhw", TAG + "关闭广告");
			adDisplay.stopAd();
			adDisplay.dismiss();
		}
	}

	public class Receiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("com.landicorp.SECOND_DISPLAY_START_AD")) {

				if (isExit) {
					return;
				}
				try {
					showADPresentation();
				} catch (Exception e) {
				}
			} else if (intent.getAction().equals("com.landicorp.SECOND_DISPLAY_STOP_AD")) {
				dismissADPresentation();
			} else if (intent.getAction().equals("com.landicorp.SECOND_DISPLAY_CLEAR_AD")) {
				if (null != adDisplay) {
					adDisplay.clearAdList();
				}
			} else if (intent.getAction().equals("com.landicorp.SECOND_DISPLAY_ADD_AD")) {
				try {
					String filePath = intent.getExtras().getString("filePath");
					if (null != filePath) {
						adList.add(filePath);
					}
				} catch (Exception e) {
					// TODO: handle exception
				}

			}
		}

	}

	private final MediaRouter.SimpleCallback mMediaRouterCallback = new MediaRouter.SimpleCallback() {
		// 当用户连接到一个媒体路由输出设备上时调用。
		@Override
		public void onRouteSelected(MediaRouter router, int type, RouteInfo info) {
			Toast.makeText(PressentationService.this, "检测到接入副显示屏", 1000).show();
		}

		// 当用户断开一个媒体路由输出设备时调用。
		@Override
		public void onRouteUnselected(MediaRouter router, int type, RouteInfo info) {
			Toast.makeText(PressentationService.this, "检测到断开副显示屏", 1000).show();
		}

		// 当展示的显示器改变现实像素，如从720p变到1080p分辨率。
		@Override
		public void onRoutePresentationDisplayChanged(MediaRouter router, RouteInfo info) {
			Toast.makeText(PressentationService.this, "副显示屏分辨率更改", 1000).show();
		}
	};

}
