package com.systekcn.guide.lyric;

import android.content.Context;
import android.util.Log;

import com.systekcn.guide.common.IConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Qiang on 2015/10/29.
 */
public class LyricDownloadManager implements IConstants {

    private static final String TAG = LyricDownloadManager.class
            .getSimpleName();
    public static final String GB2312 = "GB2312";
    public static final String UTF_8 = "utf-8";
    private final int mTimeOut = 10 * 1000;
    private final Context mContext;
    private LyricXMLParser mLyricXMLParser = new LyricXMLParser();
    private URL mUrl = null;
    private int mDownloadLyricId = -1;

    public LyricDownloadManager(Context c) {
		mContext = c;
    }

    public String searchLyricFromWeb(String lyricName,String savePath) {

        return fetchLyricContent(lyricName,savePath);
    }

    /** 根据歌词下载ID，获取网络上的歌词文本内容*/
    private String fetchLyricContent(String lyricName,String folderPath) {

        BufferedReader br = null;
        StringBuilder content = null;
        String temp = null;
        String lyricURL = BASEURL+lyricName;
        Log.i(TAG, "歌词的真实下载地址:" + lyricURL);
        try {
            mUrl = new URL(lyricURL);
        } catch (MalformedURLException e2) {
            e2.printStackTrace();
        }
        // 获取歌词文本，存在字符串类中
        try {
            // 建立网络连接
            br = new BufferedReader(new InputStreamReader(mUrl.openStream(),
                    GB2312));
            if (br != null) {
                content = new StringBuilder();
                // 逐行获取歌词文本
                while ((temp = br.readLine()) != null) {
                    content.append(temp);
                    Log.i(TAG, "<Lyric>" + temp);
                }
                br.close();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
            Log.i(TAG, "歌词获取失败");
        }

        if (content != null) {
            // 检查保存的目录是否已经创建

//			String folderPath = PreferenceManager.getDefaultSharedPreferences(
//					mContext).getString(SettingFragment.KEY_LYRIC_SAVE_PATH,
//					Constant.LYRIC_SAVE_FOLDER_PATH);
            File savefolder = new File(folderPath);
            if (!savefolder.exists()) {
                savefolder.mkdirs();
            }
            String localName=lyricName.replaceAll("/","_");
            String savePath = folderPath  + localName;
//			String savePath = folderPath + File.separator + musicName + ".lrc";
            Log.i(TAG, "歌词保存路径:" + savePath);
            saveLyric(content.toString(), savePath);
            return savePath;
        } else {
            return null;
        }

    }

    /** 将歌词保存到本地，写入外存中 */
    private void saveLyric(String content, String filePath) {
        // 保存到本地
        File file = new File(filePath);
        try {
            OutputStream outstream = new FileOutputStream(file);
            OutputStreamWriter out = new OutputStreamWriter(outstream);
            out.write(content);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "很遗憾，将歌词写入外存时发生了IO错误");
        }
        Log.i(TAG, "歌词保存成功");
    }

}
