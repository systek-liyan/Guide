package com.systek.guide.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
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
import com.systek.guide.IConstants;
import com.systek.guide.R;
import com.systek.guide.biz.DataBiz;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageLoaderUtil implements IConstants{
	private static ImageLoaderConfiguration configuration;
	private static ImageLoaderConfiguration newConfiguration(Context context)
	{
		if (configuration==null)
		{
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
			imageLoader.displayImage(imageUrl, imageView, new MyImageLoadingListener(context,imageView));
		} catch (Exception e) {
			ExceptionUtil.handleException(e);
		}
	}
	public static void displaySdcardImage(Context context, String filePathName, ImageView ivImage) {
		ImageLoader imageLoader=ImageLoader.getInstance();
		configuration=newConfiguration(context);
		imageLoader.init(configuration);
		imageLoader.displayImage("file:///"+filePathName, ivImage);
	}

	public static void displaySdcardBlurImage(Context context, String filePathName,final ImageView ivImage) {
        if(TextUtils.isEmpty(filePathName)){return;}
        Bitmap bitmap=getLoacalBitmap(filePathName);
        if(bitmap!=null){
            Bitmap mBitmap=blurBitmap(context,bitmap);//FastBlur.doBlur(bitmap,2,true)
            ivImage.setImageBitmap(mBitmap);
        }

	}

    public static void displayNetworkBlurImage(final Context context,final String imageUrl,final ImageView imageView){

        try {
            Bitmap bitmap=getImage(imageUrl);
            if(bitmap!=null){
                Bitmap mBitmap=blurBitmap(context,bitmap);
                imageView.setImageBitmap(mBitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 加载本地图片
     * http://bbs.3gstdy.com
     * @param url
     * @return
     */
    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取网络address地址对应的图片
     * @param address
     * @return bitmap的类型
     */
    public static Bitmap getImage(String address) throws Exception{
        //通过代码 模拟器浏览器访问图片的流程
        URL url = new URL(address);
        HttpURLConnection conn =  (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        //获取服务器返回回来的流
        InputStream is = conn.getInputStream();
        byte[] imagebytes = getBytes(is);
        Bitmap bitmap = BitmapFactory.decodeByteArray(imagebytes, 0, imagebytes.length);
        if(is!=null){
            is.close();
        }
        return bitmap;
    }


    public static byte[] getBytes(InputStream is) throws Exception{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while((len = is.read(buffer))!=-1){
            bos.write(buffer, 0, len);
        }
        is.close();
        bos.flush();
        byte[] result = bos.toByteArray();
        //System.out.println(new String(result));
        return  result;
    }



    private static class MyBlurImageLoadingListener implements ImageLoadingListener {

        ImageView imageView;

        Context context;

        public MyBlurImageLoadingListener(Context context, ImageView imageView) {
            this.context = context;
            this.imageView = imageView;
        }

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
            Bitmap bitmap=blurBitmap(context,loadedImage);
            imageView.setImageBitmap(bitmap);
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
    }


    private static class MyImageLoadingListener extends MyBlurImageLoadingListener{

        public MyImageLoadingListener(Context context, ImageView imageView) {
            super(context, imageView);
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
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
    }

    public static Bitmap blurBitmap(Context c,Bitmap bitmap){

        //Let's create an empty bitmap with the same size of the bitmap we want to blur
        Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        //Instantiate a new Renderscript
        RenderScript rs = RenderScript.create(c.getApplicationContext());

        //Create an Intrinsic Blur Script using the Renderscript
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        //Create the Allocations (in/out) with the Renderscript and the in/out bitmaps
        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);

        //Set the radius of the blur
        blurScript.setRadius(25f);
        //Perform the Renderscript
        blurScript.setInput(allIn);
        blurScript.forEach(allOut);

        //Copy the final bitmap created by the out Allocation to the outBitmap
        allOut.copyTo(outBitmap);

        //recycle the original bitmap
        bitmap.recycle();

        //After finishing everything, we destroy the Renderscript.
        rs.destroy();

        return outBitmap;
    }

}
