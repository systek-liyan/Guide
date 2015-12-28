package com.systekcn.guide.common.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Vibrator;
import android.util.Base64;
import android.widget.Toast;

import com.systekcn.guide.MyApplication;
import com.systekcn.guide.common.IConstants;
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

	/**震动时间*/
	public static void virbate(long time) {
		Vibrator vibrator = (Vibrator) MyApplication.get().getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(time);
	}


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
	 * 
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
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
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
	 * 
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
	 * @param context
	 *            上下文
	 * @param string
	 *            吐司要出来的内容
	 */
	public static void showMessage(Context context, String string) {
		Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
	}

	/**
	 *
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
	 * 显示对话框
	 *
	 * @param context
	 * @param message
	 *            要显示的内容
	 * @param positive
	 *            点击确定的按钮的名字
	 * @param negative
	 *            点击取消按钮的名字
	 * @param listener
	 *            对按钮的监听事件
	 */
	public static void showDialog(Context context, String message,
								  String positive, String negative,
								  DialogInterface.OnClickListener listener) {
		try {
			AlertDialog alertDialog = new AlertDialog.Builder(context).create();
			alertDialog.setMessage(message);
			alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, positive, listener);
			alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, negative, listener);
			alertDialog.show();
		} catch (Exception e) {
			ExceptionUtil.handleException(e);
		}
	}

	/**
	 * jeno_spf 用来存储保存的信息 这个方法是用来判断是否登录 其中isLogin 和 OK 当换成别的 根据自己来进行设置
	 * 当登录成功后把这个存入进去。
	 *
	 * @return
	 */
	public static boolean isLogin() {
		try {
			SharedPreferences sp = MyApplication.getAppContext().getSharedPreferences(APP_SETTING, Context.MODE_PRIVATE);
			return sp.contains("isLongin") && sp.contains("OK");
		} catch (Exception e) {
			ExceptionUtil.handleException(e);
			return false;
		}
	}

	/**
	 * 清空
	 *
	 * @param context
	 */
	public static void clearValues(Context context) {
		try {
			SharedPreferences sp = context.getSharedPreferences("jeno_spf",
					Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sp.edit();
			editor.clear();
			editor.commit();
		} catch (Exception e) {
			ExceptionUtil.handleException(e);
		}
	}

	/**
	 * 向SP文件存储数据
	 *
	 * @param context
	 * @param key
	 *            键名
	 * @param value
	 *            键值
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
			editor.commit();
		} catch (Exception e) {
			ExceptionUtil.handleException(e);
		}
	}

	/**
	 * 从SP文件中读取指定Key的值
	 *
	 * type=1/数值 defValue=-1 | type=2/字符串 defValue=null | type=3/布尔
	 * defValue=false
	 *
	 * @param context
	 * @param key
	 *            键名
	 * @param type
	 *            数据存储类型
	 * @return 键值
	 */
	public static Object getValue(Context context, String key, Object defaultObject) {
		Object object = null;
		try {
			SharedPreferences sp = context.getApplicationContext().getSharedPreferences("museum", Context.MODE_PRIVATE);
			object = null;
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
		return object;
	}

	/**
	 * 将数据保存到内存中。
	 *
	 * @param context  上下文
	 * @param fileName 文件的名字
	 * @param content  保存在文件中的内容
	 * @return
	 */
	public static boolean saveValue2Phone(Context context, String fileName,
										  String content) {

		try {
			FileOutputStream fos = new FileOutputStream(context.getFilesDir()
					+ fileName);
			fos.write(content.getBytes());
			return true;
		} catch (Exception e) {
			ExceptionUtil.handleException(e);
			return false;
		}

	}


	public static void  downLoadFromUrl(String urlStr,String fileName,String savePath) throws IOException{
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		//设置超时间为10秒
		conn.setRequestMethod("GET");
		conn.setReadTimeout(5000);
		conn.setConnectTimeout(10*1000);
		//防止屏蔽程序抓取而返回403错误
		conn.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");
		if (conn.getResponseCode() == 200) {
			//得到输入流
			InputStream inputStream = conn.getInputStream();
			//获取自己数组
			byte[] getData = readInputStream(inputStream);
			//文件保存位置
			File saveDir = new File(savePath);
			if(!saveDir.exists()){
				saveDir.mkdir();
			}
			File file = new File(saveDir+File.separator+fileName);
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(getData);
			if(fos!=null){
				fos.close();
			}
			if(inputStream!=null){
				inputStream.close();
			}
			LogUtil.i("ZHANG","文件保存成功");
		}

	}

	/**
	 * 从输入流中获取字节数组
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	private static  byte[] readInputStream(InputStream inputStream) throws IOException {
		byte[] buffer = new byte[8192];
		int len = 0;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		while((len = inputStream.read(buffer)) != -1) {
			bos.write(buffer, 0, len);
		}
		bos.close();
		return bos.toByteArray();
	}

}
