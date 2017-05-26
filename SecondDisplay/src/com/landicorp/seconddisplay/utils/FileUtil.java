package com.landicorp.seconddisplay.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import org.apache.http.util.EncodingUtils;

import android.annotation.SuppressLint;
import android.os.Environment;

@SuppressLint("NewApi")
public class FileUtil {

	static FileOutputStream fout = null;

	public static File saveFile(String filePath, String fileName, byte[] src) {
		File file = byteToFile(src, filePath, fileName);
		return file;
	}

	public static byte[] readFile(String filePath, String fileName) throws IOException {
		File file = new File(filePath + fileName);
		if (!file.exists()) {
			return null;
		}
		byte[] respone = fileToByte(file);
		return respone;
	}

	public static boolean deleteFile(String filePath) {
		boolean isDelete = false;
		File file = new File(filePath);
		if (file.exists()) {
			isDelete = file.delete();
		}
		return isDelete;
	}

	public static boolean deleteDirFile(String dirPath) {
		boolean isDelete = false;
		File file = new File(dirPath);
		if (file.exists() && file.isDirectory()) {
			if (file.delete()) {
				file.mkdirs();
				isDelete = true;
			} else {
				isDelete = false;
			}

		}
		return isDelete;
	}

	public static String getFileList(String dirPath) {
		String fileList = null;
		File file = new File(dirPath);
		if (file.exists() && file.isDirectory()) {
			File[] files = file.listFiles();
			for (File file2 : files) {
				if (file2.exists()) {
					fileList += file2.getAbsolutePath() + "|";
				}
			}
		}
		return fileList;
	}

	public static String readFromFile(String filePath) {
		String msg = null;

		try {
			FileInputStream fin = new FileInputStream(filePath);
			int length = fin.available();
			byte[] buffer = new byte[length];
			fin.read(buffer);
			msg = EncodingUtils.getString(buffer, "UTF-8");
			fin.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return msg;
	}

	public static byte[] fileToByte(File file) throws IOException {

		byte[] bytes = null;

		if (file != null)

		{
			InputStream is = new FileInputStream(file);
			int length = (int) file.length();
			if (length > Integer.MAX_VALUE) {
				return null;
			}
			bytes = new byte[length];
			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
				offset += numRead;
			}

			if (offset < bytes.length) {
				return null;
			}
			is.close();
		}

		return bytes;

	}

	public static File byteToFile(byte[] bytes, String filePath, String fileName) {
		BufferedOutputStream bos = null;
		FileOutputStream fos = null;
		File file = null;
		try {
			File dir = new File(filePath);
			if (!dir.exists() && dir.isDirectory()) {
				dir.mkdirs();
			}
			file = new File(filePath + fileName);
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);
			bos.write(bytes);

		} catch (Exception e) {

			e.printStackTrace();

		} finally {
			if (bos != null) {

				try {
					bos.close();

				} catch (IOException e1) {

					e1.printStackTrace();

				}

			}

			if (fos != null) {

				try {

					fos.close();

				} catch (IOException e1) {

					e1.printStackTrace();

				}

			}

		}
		return file;
	}

	public static void writeLogToSD(String filePath, String fileName, String msg) {
		String sdStatus = Environment.getExternalStorageState();
		if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
			return;
		}

		try {
			String pathName = filePath;
			File logFile = new File(pathName);
			logFile.mkdirs();
			logFile = new File(pathName + fileName);
			if (!logFile.exists()) {
				try {
					logFile.createNewFile();
				} catch (Exception e) {
					logFile.renameTo(new File(pathName + fileName + "_tmp"));
					logFile = new File(pathName + fileName);
					logFile.createNewFile();
				}

				fout = null;
			}
			if (null != logFile) {
				try {
					if (null == fout) {
						fout = new FileOutputStream(logFile, true);
					}
					if (null != fout) {
						byte[] bytes = msg.getBytes();
						fout.write(bytes);
						fout.flush();
					}
				} catch (Exception e) {
					fout.close();
					fout = null;
					e.printStackTrace();
				}
			}

		} catch (Exception e) {
			try {
				fout.close();
				fout = null;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();

		}

	}

	public static void writeExceptionToSD(String filePath, String fileName, String msg) {
		String sdStatus = Environment.getExternalStorageState();
		if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {

			return;
		}
		try {
			String pathName = filePath;

			File exceptionLogFile = new File(pathName);
			exceptionLogFile.mkdirs();
			exceptionLogFile = new File(pathName + fileName);
			if (!exceptionLogFile.exists()) {
				exceptionLogFile.createNewFile();
			}
			if (null != exceptionLogFile) {
				OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(exceptionLogFile, true), "UTF-8");
				BufferedWriter writer = new BufferedWriter(write);
				writer.write(msg);
				writer.close();
			}

		} catch (Exception e) {
			e.printStackTrace();

		}
	}

}
