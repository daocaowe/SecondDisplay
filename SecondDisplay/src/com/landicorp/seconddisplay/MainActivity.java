package com.landicorp.seconddisplay;

import com.landicorp.seconddisplay.services.PressentationService;
import com.landicorp.seconddisplay.views.MessageDisplay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaRouter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener {
	Intent pressentationService;// 拓展屏幕服务
	private final int SELECT_REQUEST_CODE = 0;
	private MessageDisplay messageDisplay = null;
	private MediaRouter mMediaRouter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// 初始化mMediaRouter，用于辅屏幕显示路由
		mMediaRouter = (MediaRouter) getSystemService(Context.MEDIA_ROUTER_SERVICE);

		// 启动辅屏幕广告后台服务
		pressentationService = new Intent(this, PressentationService.class);
		startService(pressentationService);

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// 停止辅屏幕广告服务
		stopService(pressentationService);
	}

	/**
	 * 添加广告
	 * 
	 * @param v
	 */

	public void addAd(View v) {
		Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT);
		innerIntent.setType("image/*,video/*");
		Intent wrapperIntent = Intent.createChooser(innerIntent, null);
		startActivityForResult(wrapperIntent, SELECT_REQUEST_CODE);
	}

	/**
	 * 清除广告
	 * 
	 * @param v
	 */
	public void clearAd(View v) {
		sendBroadcast(new Intent("com.landicorp.SECOND_DISPLAY_CLEAR_AD"));
	}

	/**
	 * 播放广告
	 * 
	 * @param v
	 */
	public void playAd(View v) {
		sendBroadcast(new Intent("com.landicorp.SECOND_DISPLAY_START_AD"));
	}

	/**
	 * 停止播放广告
	 * 
	 * @param v
	 */
	public void stopAd(View v) {
		Toast.makeText(this, "停止播放广告", Toast.LENGTH_SHORT).show();
		sendBroadcast(new Intent("com.landicorp.SECOND_DISPLAY_STOP_AD"));
	}

	/**
	 * 显示辅屏幕对话框
	 * 
	 * @param v
	 */
	public void showMessage(View v) {
		showMessageDisplay();
	}

	/**
	 * 关闭辅屏幕对话框
	 */
	public void dismissMessage() {
		if (null != messageDisplay) {
			messageDisplay.dismiss();
		}
	}

	public void exit(View v) {
		finish();
	}

	/**
	 * 在Activity中直接启动显示屏幕，副显示屏周期跟Activity一起销毁而退出。
	 */
	private void showMessageDisplay() {
		// 停止播放广告
		sendBroadcast(new Intent("com.landicorp.SECOND_DISPLAY_STOP_AD"));

		MediaRouter.RouteInfo route = mMediaRouter.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
		Display presentationDisplay = route != null ? route.getPresentationDisplay() : null;
		if (presentationDisplay == null) {
			Toast.makeText(this, "未检测到辅显示屏", Toast.LENGTH_SHORT).show();
			return;
		}
		messageDisplay = new MessageDisplay(this, presentationDisplay);
		try {
			messageDisplay.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
			messageDisplay.setContentView(R.layout.pressentation_message);
			Button okBtn = (Button) messageDisplay.findViewById(R.id.okBtn);
			Button cancelBtn = (Button) messageDisplay.findViewById(R.id.cancelBtn);
			messageDisplay.show();
			okBtn.setOnClickListener(this);
			cancelBtn.setOnClickListener(this);
			Toast.makeText(this, "显示信息对话框", Toast.LENGTH_SHORT).show();
		} catch (WindowManager.InvalidDisplayException ex) {
			messageDisplay = null;
		}
	}

	@Override
	public void onClick(View v) {
		// 继续播放广告
		sendBroadcast(new Intent("com.landicorp.SECOND_DISPLAY_START_AD"));
		// 关闭对话框
		dismissMessage();
		switch (v.getId()) {
		case R.id.okBtn:
			Toast.makeText(this, "用户点击了【确定按钮】", Toast.LENGTH_SHORT).show();
			break;
		case R.id.cancelBtn:
			Toast.makeText(this, "用户点击了【取消按钮】", Toast.LENGTH_SHORT).show();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && requestCode == SELECT_REQUEST_CODE) {
			Uri mUri = data.getData();
			if (mUri != null) {
				// 获取广告文件路径
				String filepath = getRealPathFromURI(mUri);
				System.out.println("filepath-------------->" + filepath);
				// 添加广告
				Intent intent = new Intent("com.landicorp.SECOND_DISPLAY_ADD_AD");
				intent.putExtra("filePath", filepath);
				sendBroadcast(intent);
			}
		}
	}

	public String getRealPathFromURI(Uri contentUri) {
		String res = null;
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
		if (cursor.moveToFirst()) {
			;
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			res = cursor.getString(column_index);
		}
		cursor.close();
		return res;
	}

}
