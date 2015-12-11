package com.systekcn.guide.custom.imageacquire;

import android.graphics.Bitmap;

import java.io.File;

/**
 * 图片获取请求回调
 * 
 * @author "JTech"
 *
 */
public interface OnImageAcquire {
	/**
	 * 加载成功
	 * 
	 * @param file
	 *            图片的本地路径
	 * @param bitmap
	 *            图片对象
	 */
	void LoadOk(File file, Bitmap bitmap);

	/**
	 * 加载失败
	 * 
	 * @param err
	 *            异常
	 */
	void LoadFail(String err);
}