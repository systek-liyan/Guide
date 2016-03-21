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
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.systek.guide.IConstants;
import com.systek.guide.MyApplication;
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
    private static DisplayImageOptions roundImageOptions;
    private static DisplayImageOptions normalOptions;


    private static synchronized ImageLoaderConfiguration newConfiguration(Context context) {
        context=context.getApplicationContext();
        if (configuration==null) {
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


    private static synchronized DisplayImageOptions newNormalDisplayOptions(){
        if(normalOptions==null){
            // 使用DisplayImageOptions.Builder()创建DisplayImageOptions
            normalOptions = new DisplayImageOptions.Builder()
                    //.showStubImage(R.drawable.ic_stub)          // 设置图片下载期间显示的图片
                    //.showImageForEmptyUri(R.drawable.ic_empty)  // 设置图片Uri为空或是错误的时候显示的图片
                    //.showImageOnFail(R.drawable.ic_error)       // 设置图片加载或解码过程中发生错误显示的图片
                    .cacheInMemory()                        // 设置下载的图片是否缓存在内存中
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .cacheOnDisc()                          // 设置下载的图片是否缓存在SD卡中
                    .build();
        }
        return normalOptions;
    }

    private static synchronized DisplayImageOptions newRoundDisplayOptions(){
        if(roundImageOptions==null){
            // 使用DisplayImageOptions.Builder()创建DisplayImageOptions
            roundImageOptions = new DisplayImageOptions.Builder()
                    //.showStubImage(R.drawable.ic_stub)          // 设置图片下载期间显示的图片
                    //.showImageForEmptyUri(R.drawable.ic_empty)  // 设置图片Uri为空或是错误的时候显示的图片
                    //.showImageOnFail(R.drawable.ic_error)       // 设置图片加载或解码过程中发生错误显示的图片
                    .cacheInMemory()                        // 设置下载的图片是否缓存在内存中
                    .cacheOnDisc()                          // 设置下载的图片是否缓存在SD卡中
                    .displayer(new RoundedBitmapDisplayer(20))  // 设置成圆角图片
                    .build();
        }
        return roundImageOptions;
    }

    public static void displayNetworkImage(final Context context,final String imageUrl,final ImageView imageView)
    {
        try {
            ImageLoader imageLoader=ImageLoader.getInstance();
            configuration=newConfiguration(context);
            imageLoader.init(configuration);
            //imageLoader.displayImage(imageUrl, imageView);
            imageLoader.displayImage(imageUrl, imageView,normalOptions, new MyImageLoadingListener(context,imageView));
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
        }
    }
    public static void displaySdcardImage(final Context context, String filePathName,final ImageView ivImage) {
        ImageLoader imageLoader=ImageLoader.getInstance();
        configuration=newConfiguration(context);
        roundImageOptions=newRoundDisplayOptions();
        imageLoader.init(configuration);
        imageLoader.displayImage("file:///" + filePathName, ivImage,normalOptions);
    }

    public static void displaySdcardBlurImage(final Context context, String filePathName,final ImageView ivImage) {

        ImageLoader imageLoader=ImageLoader.getInstance();
        configuration=newConfiguration(context);
        imageLoader.init(configuration);
        imageLoader.displayImage("file:///" + filePathName, ivImage,normalOptions, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                Bitmap mBitmap = blurBitmap(context, bitmap);//FastBlur.doBlur(bitmap,2,true)
                ivImage.setImageBitmap(mBitmap);
            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        });

    }

    public static void displayNetworkBlurImage(final Context context,final String imageUrl,final ImageView imageView){

        try {
            ImageLoader.getInstance().loadImage(imageUrl, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {

                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    if(bitmap!=null){
                        Bitmap mBitmap= blurBitmap(context,bitmap);//FastBlur.doBlur(bitmap,2,true)
                        imageView.setImageBitmap(mBitmap);
                    }
                }

                @Override
                public void onLoadingCancelled(String s, View view) {

                }
            });
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
            //imageView.setImageResource(R.drawable.emotionstore_progresscancelbtn);
            LogUtil.i("图片加载失败",failReason.toString());
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            Bitmap bitmap=blurBitmap(context,loadedImage);
            imageView.setImageBitmap(bitmap);
            try{
                LogUtil.i("ZHANG","onLoadingComplete");
                if(!imageUri.startsWith(BASE_URL)){return;}
                String path=imageUri.substring(imageUri.indexOf(BASE_URL) + BASE_URL.length());
                String name=Tools.changePathToName(path);
                String museumId= DataBiz.getCurrentMuseumId();
                if(TextUtils.isEmpty(museumId)){return;}
                String savePath=LOCAL_ASSETS_PATH+museumId+"/";
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
                //LogUtil.i("ZHANG","onLoadingComplete");
                if(!imageUri.startsWith(BASE_URL)){return;}
                String path=imageUri.substring(imageUri.indexOf(BASE_URL) + BASE_URL.length());
                String name=Tools.changePathToName(path);
                String museumId= DataBiz.getCurrentMuseumId();
                if(TextUtils.isEmpty(museumId)){return;}
                String savePath=LOCAL_ASSETS_PATH+museumId+"/";
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

    public static void displayImage(String iconPath,ImageView imageView){
        String name= Tools.changePathToName(iconPath);
        String path=LOCAL_ASSETS_PATH+DataBiz.getCurrentMuseumId()+"/"+name;
        if(Tools.isFileExist(path)){
            ImageLoaderUtil.displaySdcardImage(MyApplication.get(), path, imageView);
        }else{
            ImageLoaderUtil.displayNetworkImage(MyApplication.get(), BASE_URL + iconPath,imageView);
        }

    }
    public static void displayImage(String iconPath,String museumId,ImageView imageView){
        String name= Tools.changePathToName(iconPath);
        String path=LOCAL_ASSETS_PATH+museumId+"/"+name;
        if(Tools.isFileExist(path)){
            ImageLoaderUtil.displaySdcardImage(MyApplication.get(), path, imageView);
        }else{
            ImageLoaderUtil.displayNetworkImage(MyApplication.get(), BASE_URL + iconPath,imageView);
        }

    }

}
