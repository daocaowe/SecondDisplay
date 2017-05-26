package com.landicorp.seconddisplay;

import java.io.File;

import com.landicorp.seconddisplay.common.Common;

import android.app.Application;
import android.os.Environment;

public class SecondDisplayApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		// 创建工作目录
		Common.AD_PATH = Environment.getExternalStorageDirectory() + "/" + getApplicationContext().getPackageName() + "/";
		File file = new File(Common.AD_PATH);
		if (!file.exists()) {
			file.mkdirs();
		}
		// 创建广告目录
		file = new File(Common.AD_PATH);
		if (!file.exists()) {
			file.mkdirs();
		}

	}
}
