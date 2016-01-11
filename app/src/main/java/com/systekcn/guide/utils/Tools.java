package com.systekcn.guide.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.systekcn.guide.IConstants;
import com.systekcn.guide.entity.CityBean;
import com.systekcn.guide.entity.ExhibitBean;
import com.systekcn.guide.entity.MuseumBean;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;

public class Tools implements IConstants{

	/**震动时间
	public static void virbate(long time) {
		Vibrator vibrator = (Vibrator) MyApplication.get().getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(time);
	}*/


	/**
	 * 从Assets中读取图片
	 */
	public Bitmap getImageFromAssetsFile(Context context,String fileName)
	{
		Bitmap image = null;
		AssetManager am = context.getResources().getAssets();
		try
		{
			InputStream is = am.open(fileName);
			image = BitmapFactory.decodeStream(is);
			is.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return image;

	}

	public static  byte[] BitmapToBytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}
	public static void createOrCheckFolder(String path) {
		File mPath = new File(path);
		if (!mPath.exists()) {
			mPath.mkdirs();
		}
	}


	public static String  changePathToName(String path){
		return path .replaceAll("/","_");
	}


	public static byte[] getImageFromNet(URL url) {
		byte[] data = null;
		try {
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setReadTimeout(5000);
			InputStream input = conn.getInputStream();// 到这可以直接BitmapFactory.decodeFile也行。 返回bitmap
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = input.read(buffer)) != -1) {
				output.write(buffer, 0, len);
			}
			input.close();
			data = output.toByteArray();
			System.out.println("下载完毕！");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			ExceptionUtil.handleException(e);
		}
		return data;
	}


	/**
	 * 把bitmap转换成String
	 * 
	 * @param filePath
	 * @return
	 */
	public static String bitmapToString(String filePath,int width,int height) {

		Bitmap bm = getSmallBitmap(filePath,width,height);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.JPEG, 40, baos);
		byte[] b = baos.toByteArray();
		return Base64.encodeToString(b, Base64.DEFAULT);

	}

	/**
	 * 计算图片的缩放值
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			// Calculate ratios of height and width to requested height and
			// width
			final int heightRatio = Math.round((float) height/(float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}

	/**
	 * 根据路径获得突破并压缩返回bitmap用于显示
	 * @return
	 */
	public static Bitmap getSmallBitmap(String filePath,int width,int height) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);
		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, width,height);
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(filePath, options);
	}

	public static String checkTypeForNetUrl(int type) {
		String url=null;
		if (type==URL_TYPE_GET_CITY) {
			url=URL_CITY_LIST;
		} else if (type==URL_TYPE_GET_MUSEUM_LIST) {
			url=URL_MUSEUM_LIST;
		} else if (type==URL_TYPE_GET_EXHIBITS_BY_MUSEUM_ID) {
			url=URL_EXHIBIT_LIST;
		}else if(type==URL_TYPE_GET_MUSEUM_BY_ID){
			url=URL_GET_MUSEUM_BY_ID;
		}
		return url;
	}

	public static Class<?> checkTypeForClass(int type){
		if(type==URL_TYPE_GET_CITY){
			return CityBean.class;
		}else if(type==URL_TYPE_GET_MUSEUM_LIST||type==URL_TYPE_GET_MUSEUM_BY_ID){
			return MuseumBean.class;
		}else if(type==URL_TYPE_GET_EXHIBITS_BY_MUSEUM_ID){
			return ExhibitBean.class;
		}else if(type==URL_TYPE_GET_EXHIBIT_BY_EXHIBIT_ID){
			return ExhibitBean.class;
		}
		return null;
	}

	public static boolean isFileExist(String path){
		File file =new File(path);
		return file.exists();
	}
	/**
	 * @param str
	 *            密码字符串
	 * @return 加密后的字符串
	 */
	public static String MD5(String str) {
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		char[] charArray = str.toCharArray();
		byte[] byteArray = new byte[charArray.length];
		for (int i = 0; i < charArray.length; i++) {
			byteArray[i] = (byte) charArray[i];
		}
		byte[] md5Bytes = md5.digest(byteArray);
		StringBuffer hexValue = new StringBuffer();
		for (int i = 0; i < md5Bytes.length; i++) {
			int val = ((int) md5Bytes[i]) & 0xff;
			if (val < 16) {
				hexValue.append("0");
			}
			hexValue.append(Integer.toHexString(val));
		}
		return hexValue.toString();
	}


	/**
	 * 清空
	 * @param context
	 */
	public static void clearValues(Context context) {
		try {
			SharedPreferences sp = context.getSharedPreferences("museum",Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sp.edit();
			editor.clear();
			editor.commit();
		} catch (Exception e) {
			ExceptionUtil.handleException(e);
		}
	}

	/**
	 * 向SP文件存储数据
	 * @param context
	 * @param key  键名
	 * @param value  键值
	 */
	public static void saveValue(Context context, String key, Object value) {
		try {
			SharedPreferences sp = context.getApplicationContext().getSharedPreferences("museum", Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sp.edit();
			if (value instanceof Integer) {
				editor.putInt(key, (Integer) value);
			} else if (value instanceof String) {
				editor.putString(key, (String) value);
			} else if (value instanceof Boolean) {
				editor.putBoolean(key, (Boolean) value);
			}
			editor.apply();
		} catch (Exception e) {
			ExceptionUtil.handleException(e);
		}
	}

	/**
	 * 从SP文件中读取指定Key的值
	 * type=1/数值 defValue=-1 | type=2/字符串 defValue=null | type=3/布尔
	 * defValue=false
	 * @param context
	 * @param key  键名
	 * @return 键值
	 */
	public static Object getValue(Context context, String key, Object defaultObject) {
		try {
			SharedPreferences sp = context.getApplicationContext().getSharedPreferences("museum", Context.MODE_PRIVATE);
			if (defaultObject instanceof Integer) {
				return sp.getInt(key, (Integer) defaultObject);
			} else if (defaultObject instanceof String) {
				return sp.getString(key, (String) defaultObject);
			} else if (defaultObject instanceof Boolean) {
				return sp.getBoolean(key, (Boolean) defaultObject);
			}
		} catch (Exception e) {
			ExceptionUtil.handleException(e);
		}
		return null;
	}

	/**
	 * 将数据保存到内存中。
	 * @param context  上下文
	 * @param fileName 文件的名字
	 * @param content  保存在文件中的内容
	 * @return
	 */
	public static boolean saveValue2Phone(Context context, String fileName, String content) {

		try {
			FileOutputStream fos = new FileOutputStream(context.getFilesDir() + fileName);
			fos.write(content.getBytes());
			return true;
		} catch (Exception e) {
			ExceptionUtil.handleException(e);
			return false;
		}

	}

}
