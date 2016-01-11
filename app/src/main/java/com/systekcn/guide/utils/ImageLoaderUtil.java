package com.systekcn.guide.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.systekcn.guide.IConstants;
import com.systekcn.guide.R;
import com.systekcn.guide.biz.DataBiz;

import java.io.File;

public class ImageLoaderUtil implements IConstants{
	private static ImageLoaderConfiguration configuration;
	private static ImageLoaderConfiguration newConfiguration(Context context)
	{
		if (configuration==null)
		{
			//configuration=new ImageLoaderConfiguration.Builder(context).diskCacheSize(1024*1024*100).diskCacheFileCount(100).build();

			configuration = new ImageLoaderConfiguration
					.Builder(context)
					.memoryCacheExtraOptions(480, 800) // max width, max height，即保存的每个缓存文件的最大长宽
					//.discCacheExtraOptions(480, 800, CompressFormat.JPEG, 75, null) // Can slow ImageLoader, use it carefully (Better don't use it)/设置缓存的详细信息，最好不要设置这个
					.threadPoolSize(3)//线程池内加载的数量
					.threadPriority(Thread.NORM_PRIORITY - 2)
					.denyCacheImageMultipleSizesInMemory()
					.memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024)) // You can pass your own memory cache implementation/你可以通过自己的内存缓存实现
					//.memoryCache((MemoryCacheAware<String, Bitmap>) new LruMemoryCache(2 * 1024 * 1024))
					.memoryCacheSize(2 * 1024 * 1024)
					.discCacheSize(50 * 1024 * 1024)
					.discCacheFileNameGenerator(new Md5FileNameGenerator())//将保存的时候的URI名称用MD5 加密
					.tasksProcessingOrder(QueueProcessingType.LIFO)
					.discCacheFileCount(100) //缓存的文件数量
							//  .discCache(new UnlimitedDiscCache(cacheDir))//自定义缓存路径
					.defaultDisplayImageOptions(DisplayImageOptions.createSimple())
					.imageDownloader(new BaseImageDownloader(context, 5 * 1000, 30 * 1000)) // connectTimeout (5 s), readTimeout (30 s)超时时间
							//.writeDebugLogs() // Remove for release app
					.build();//开始构建
		}
		return configuration;
	}
	public static void displayNetworkImage(final Context context,final String imageUrl,final ImageView imageView)
	{
		try {
			ImageLoader imageLoader=ImageLoader.getInstance();
			configuration=newConfiguration(context);
			imageLoader.init(configuration);
			//imageLoader.displayImage(imageUrl, imageView);
			imageLoader.displayImage(imageUrl, imageView, new ImageLoadingListener() {

				@Override
				public void onLoadingStarted(String imageUri, View view) {
				}

				@Override
				public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
					imageView.setImageResource(R.drawable.emotionstore_progresscancelbtn);
					LogUtil.i("图片加载失败",failReason.toString());
				}

				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					imageView.setImageBitmap(loadedImage);
                    try{
                        if(!imageUri.startsWith(BASE_URL)){return;}
                        String path=imageUri.substring(imageUri.indexOf(BASE_URL) + BASE_URL.length());
                        String name=Tools.changePathToName(path);
                        String museumId= DataBiz.getCurrentMuseumId();
                        if(TextUtils.isEmpty(museumId)){return;}
                        String savePath=APP_ASSETS_PATH+museumId+"/"+LOCAL_FILE_TYPE_IMAGE;
                        File dir=new File(savePath);
                        if(!dir.exists()){dir.mkdirs();}
                        DataBiz.saveBitmap(savePath, name, loadedImage);
                    }catch (Exception e){ExceptionUtil.handleException(e);}
				}
				@Override
				public void onLoadingCancelled(String imageUri, View view) {
					LogUtil.i("图片加载取消", imageUri);
                }
			});
		} catch (Exception e) {
			ExceptionUtil.handleException(e);
		}
	}
	public static void displaySdcardImage(Context context, String filePathName,
										  ImageView ivImage) {
		ImageLoader imageLoader=ImageLoader.getInstance();
		configuration=newConfiguration(context);
		imageLoader.init(configuration);
		imageLoader.displayImage("file:///"+filePathName, ivImage);
	}

}
