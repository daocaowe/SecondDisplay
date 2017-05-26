package com.landicorp.player;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class ImageUtil {

	private static ImageUtil mImageLoader = null;

	public static ImageUtil getInstance() {
		if (mImageLoader == null) {
			mImageLoader = new ImageUtil();
		}
		return mImageLoader;
	}

	private DisplayImageOptions mImageOptions = null;

	public void create(Context context) {
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory().memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024))
		// 可以通过自己的内存缓存实现
				.memoryCacheSize(2 * 1024 * 1024).diskCache(new UnlimitedDiscCache(context.getFilesDir()))
				// 不缓存在SD卡，有可能被拔出
				.diskCacheFileNameGenerator(new Md5FileNameGenerator()).diskCacheSize(50 * 1024 * 1024) // 50
																										// Mb
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				// .writeDebugLogs() // Remove for release app
				.build();
		ImageLoader.getInstance().init(config);
	}

	public void destory() {
		if (ImageLoader.getInstance().isInited()) {
			ImageLoader.getInstance().stop();
		}
	}

	public void clearCache() {
		if (ImageLoader.getInstance().isInited()) {
			ImageLoader.getInstance().clearMemoryCache();
			ImageLoader.getInstance().clearDiskCache();
		}
	}

	public boolean showImage(ImageView img, String path, ImageLoadingListener listener) {
		if (img == null)
			return false;
		if (ImageLoader.getInstance().isInited()) {

			ImageLoader.getInstance().displayImage(path, img, getImageOptions(), listener);
			return true;
		}
		return false;
	}

	public boolean showImage(ImageView img, String path) {
		if (img == null)
			return false;
		if (ImageLoader.getInstance().isInited()) {

			ImageLoader.getInstance().displayImage(path, img, getImageOptions());
			return true;
		}
		return false;
	}

	private DisplayImageOptions getImageOptions() {
		if (mImageOptions == null) {
			mImageOptions = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565).build();
		}
		return mImageOptions;
	}

}
