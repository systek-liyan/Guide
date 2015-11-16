package com.systekcn.guide.common.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.entity.CityBean;
import com.systekcn.guide.entity.ExhibitBean;
import com.systekcn.guide.entity.MuseumBean;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Tools implements IConstants{

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
		String name =path .replaceAll("/","_");
		return name;
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
			e.printStackTrace();
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
		}
		return url;
	}

	public static Class<?> checkTypeForClass(int type){
		if(type==URL_TYPE_GET_CITY){
			return CityBean.class;
		}else if(type==URL_TYPE_GET_MUSEUM_LIST){
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
		if(file.exists()){
			return true;
		}else{
			return false;
		}
	}

}
