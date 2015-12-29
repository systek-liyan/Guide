package com.systekcn.guide.common.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Qiang on 2015/12/29.
 */
public class MyHttpUtil {

    /**
     *
     */
    public static String get(String path) {
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
    public static String post(String path,String content) {
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
            conn.setRequestProperty("contentType","application/x-www-form-urlencoded");
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
}
