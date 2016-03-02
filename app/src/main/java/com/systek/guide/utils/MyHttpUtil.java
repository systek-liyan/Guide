package com.systek.guide.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Qiang on 2015/12/29.
 */
public class MyHttpUtil {






    /**
     * Get请求，获得返回数据
     *
     * @param urlStr
     * @return
     * @throws Exception
     */
    public static String doGet(String urlStr)
    {
        URL url = null;
        HttpURLConnection conn = null;
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        try
        {
            url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            if (conn.getResponseCode() == 200)
            {
                is = conn.getInputStream();
                baos = new ByteArrayOutputStream();
                int len = -1;
                byte[] buf = new byte[128];

                while ((len = is.read(buf)) != -1)
                {
                    baos.write(buf, 0, len);
                }
                baos.flush();
                return baos.toString();
            } else
            {
                throw new RuntimeException(" responseCode is not 200 ... ");
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                if (is != null)
                    is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try
            {
                if (baos != null)
                    baos.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            if(conn!=null) {
                conn.disconnect();
            }
        }

        return null ;

    }

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url
     *            发送请求的 URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     * @throws Exception
     */
    public static String doPost(String url, String param)
    {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try
        {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            HttpURLConnection conn = (HttpURLConnection) realUrl
                    .openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            conn.setRequestProperty("charset", "utf-8");
            conn.setUseCaches(false);
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(5000);

            if (param != null && !param.trim().equals(""))
            {
                // 获取URLConnection对象对应的输出流
                out = new PrintWriter(conn.getOutputStream());
                // 发送请求参数
                out.print(param);
                // flush输出流的缓冲
                out.flush();
            }
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null)
            {
                result += line;
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        // 使用finally块来关闭输出流、输入流
        finally
        {
            try
            {
                if (out != null)
                {
                    out.close();
                }
                if (in != null)
                {
                    in.close();
                }
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
        return result;
    }






    /**
     *  发送 http get 请求
     */
    public static String sendGet(String path) {
        HttpURLConnection conn = null;
        String response = "";
        try {
            URL url = new URL(path);
            //1.得到HttpURLConnection实例化对象
            conn = (HttpURLConnection) url.openConnection();
            //2.设置请求信息（请求方式... ...）
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept-Encoding", "gzip");	//设置头参数
            //设置请求方式和响应时间
            conn.setConnectTimeout(5000);
            /*设置允许输出*/
            conn.setDoOutput(true);
            conn.setRequestProperty("encoding", "UTF-8"); //可以指定编码
            //不使用缓存
            conn.setUseCaches(false);
            //3.读取相应
            if (conn.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                InputStream is = conn.getInputStream();
                String encoding = conn.getContentEncoding();
                if(encoding!=null && encoding.contains("gzip")) {//首先判断服务器返回的数据是否支持gzip压缩，
                    is = new GZIPInputStream(is);    //如果支持则应该使用GZIPInputStream解压，否则会出现乱码无效数据*/
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String readLine = null;
                while((readLine =br.readLine()) != null){
                    //response = br.readLine();
                    response = response + readLine;
                }
                is.close();
                br.close();
                conn.disconnect();
            } else {
                response = "";
            }
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
        } finally {
            //4.释放资源
            if (conn != null) {
                //关闭连接 即设置 http.keepAlive = false;
                conn.disconnect();
            }
        }
        return response;
    }
    /**
     * post请求方式，完成form表单的提交
     */
    public static String sendPost(String path,String content) {
        HttpURLConnection conn = null;
        String response="";
        try {
            URL url = new URL(path);
            //1.得到HttpURLConnection实例化对象
            conn = (HttpURLConnection) url.openConnection();
            //2.设置请求方式
            conn.setRequestMethod("POST");
            //3.设置post提交内容的类型和长度
		/*
		 * 只有设置contentType为application/x-www-form-urlencoded，
		 * servlet就可以直接使用request.getParameter("username");直接得到所需要信息
		 */
            conn.setRequestProperty("contentType", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(content.getBytes().length));
            //默认为false
            conn.setDoOutput(true);
            //4.向服务器写入数据
            conn.getOutputStream().write(content.getBytes());
            //5.得到服务器相应
            if (conn.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                InputStream is = conn.getInputStream();
                String encoding = conn.getContentEncoding();
                if(encoding!=null && encoding.contains("gzip")) {//首先判断服务器返回的数据是否支持gzip压缩，
                    is = new GZIPInputStream(is);    //如果支持则应该使用GZIPInputStream解压，否则会出现乱码无效数据*/
                }
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String readLine = null;
                while((readLine =br.readLine()) != null){
                    //response = br.readLine();
                    response = response + readLine;
                }
                is.close();
                br.close();
                conn.disconnect();
            } else {
                response = "";
            }
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
        } finally {
            //6.释放资源
            if (conn != null) {
                //关闭连接 即设置 http.keepAlive = false;
                conn.disconnect();
            }
        }
        return response;
    }

    /**
     * post方式，完成文件的上传
     */
    private static boolean uploadFile(String filePath,String url) {
        boolean isUpLoadTrue=false;
        HttpURLConnection conn = null;
        OutputStream out = null;
        InputStream in = null;
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(filePath);
            //1.得到HttpURLConnection实例化对象
            conn = (HttpURLConnection) new URL(url).openConnection();
            //2.设置请求方式
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            //不使用缓存
            conn.setUseCaches(false);
            //conn.setRequestProperty("Range", "bytes="+start+"-"+end);多线程请求部分数据
            //3.设置请求头属性
            //上传文件的类型 rard Mime-type为application/x-rar-compressed
            conn.setRequestProperty("content-type", "application/x-rar-compressed");
		/*
		 * (1).在已知文件大小，需要上传大文件时，应该设置下面的属性，即文件长度
		 * 	当文件较小时，可以设置头信息即conn.setRequestProperty("content-length", "文件字节长度大小");
		 * (2).在文件大小不可知时，使用setChunkedStreamingMode();
		 */
            conn.setFixedLengthStreamingMode(fin.available());
            String fileName = filePath.substring(filePath.lastIndexOf("\\")+1);
            //可以将文件名称信息已头文件方式发送，在servlet中可以使用request.getHeader("filename")读取
            conn.setRequestProperty("filename", fileName);

            //4.向服务器中发送数据
            out = new BufferedOutputStream(conn.getOutputStream());
            long totalSize = fin.available();
            long currentSize = 0;
            int len = -1;
            byte[] bytes = new byte[1024*5];
            while ((len = fin.read(bytes)) != -1) {
                out.write(bytes);
                currentSize += len;
                System.out.println("已经长传:"+(int)(currentSize*100/(float)totalSize)+"%");
            }
            isUpLoadTrue=true;
            System.out.println("上传成功！");
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            //5.释放相应的资源
            if(conn != null){
                conn.disconnect();
            }
        }
        return isUpLoadTrue;
    }


    private static final int BUFFER_SIZE = 4096;

    /**
     *  Downloads a file from a URL
     * @param urlStr HTTP URL of the file to be downloaded
     * @param savePath path of the directory to save the file
     * @throws IOException
     */
    public static void  downLoadFromUrl(String urlStr,String savePath,String fileName) throws IOException{
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        //设置超时间为10秒
        conn.setRequestMethod("GET");
        conn.setReadTimeout(5000);
        conn.setConnectTimeout(20*1000);
        //防止屏蔽程序抓取而返回403错误
        //conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");
        if (conn.getResponseCode() == 200) {

            InputStream inputStream = conn.getInputStream();
            File saveDir=new File(savePath);
            if(!saveDir.exists()){
                saveDir.mkdirs();
            }
            String saveFile = saveDir + File.separator + fileName;
            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(saveFile);
            int bytesRead = -1;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
            LogUtil.i("ZHANG","File downloaded=="+fileName);

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
