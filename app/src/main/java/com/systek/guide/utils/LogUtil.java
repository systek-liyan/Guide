package com.systek.guide.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.systek.guide.BuildConfig;
import com.systek.guide.MyApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;


/**
 * Created by Qiang on 2015/11/26.
 *
 * 日志统一处理
 */
public class LogUtil {

    public static void i(String tag,Object msg)
    {
        if (MyApplication.isRelease)
        {
            return;
        }
        Log.i(tag, String.valueOf(msg));
    }

    public static void e(String tag,Object msg)
    {
        if (MyApplication.isRelease)
        {
            return;
        }
        Log.e(tag, String.valueOf(msg));
    }

    private static final String TAG = "CMBC";
    private static final int MESSAGE_LENGTH_MAX = 256;
    private static final int mMaxArrayCount = 100;// 每100条向文件里保存一次
    private static final int mMaxLogLength = 5 * 1024 * 1024; // 最大5M
    private static final Object mLock = new Object();

    private static ArrayList<String> mLogArray = new ArrayList<String>();
    private static final String LOG_FILE_NAME = "log.txt";

    private static boolean mIsPrintLog = true;
    private static Context mContext;


    public static void isPrintLog( boolean printLog ) {

        mIsPrintLog = printLog;
    }


    public static void setContext( Context context ) {

        mContext = context;
    }


    /**
     * Priority constant for the println method; use Log.v.
     */
    public static final int VERBOSE = 2;

    /**
     * Priority constant for the println method; use Log.d.
     */
    public static final int DEBUG = 3;

    /**
     * Priority constant for the println method; use Log.i.
     */
    public static final int INFO = 4;

    /**
     * Priority constant for the println method; use Log.w.
     */
    public static final int WARN = 5;

    public static final int ERROR = 6;


    private static void log( final String strLog ) {

        new Thread( new Runnable() {

            @Override
            public void run() {

                synchronized( mLock ) {
                    mLogArray.add( TimeUtil.getNowTimeString() + "\t" + strLog + "\r\n" );
                    if( mLogArray.size() >= mMaxArrayCount ) {
                        commit();
                        mLogArray.clear();
                    }
                }
            }
        } ).start();
    }


    private static void commit() {

        if( mIsPrintLog ) {
            synchronized( mLock ) {
                commitLog();
            }
        }
    }


    public static void d( String tag, String msg ) {

        if( null == msg ) {
            return;
        }
        print( DEBUG, tag, msg );
        if( !mIsPrintLog ) {
            log( tag + "\t" + ( msg.length() > MESSAGE_LENGTH_MAX ? msg.substring( MESSAGE_LENGTH_MAX ) : msg ) );
        }
    }


    public static void i( String tag, String msg ) {

        if( null == msg ) {
            return;
        }
        String finalTag = TAG + tag;
        print( INFO, finalTag, msg );
        if( mIsPrintLog ) {
            log( finalTag + "\t" + ( msg.length() > MESSAGE_LENGTH_MAX ? msg.substring( MESSAGE_LENGTH_MAX ) : msg ) );
        }
    }


    public static void v( String tag, String msg ) {

        if( null == msg ) {
            return;
        }
        String finalTag = TAG + tag;
        print( VERBOSE, finalTag, msg );
        if( mIsPrintLog ) {
            log( finalTag + "\t" + ( msg.length() > MESSAGE_LENGTH_MAX ? msg.substring( MESSAGE_LENGTH_MAX ) : msg ) );
        }
    }


    public static void w( String tag, String msg ) {

        if( null == msg ) {
            return;
        }
        String finalTag = TAG + tag;
        print( WARN, finalTag, msg );
        if( mIsPrintLog ) {
            log( finalTag + "\t" + ( msg.length() > MESSAGE_LENGTH_MAX ? msg.substring( MESSAGE_LENGTH_MAX ) : msg ) );
        }
    }


    public static void e( String tag, String msg ) {

        if( null == msg ) {
            return;
        }
        String finalTag = TAG + tag;
        print( ERROR, finalTag, msg );
        if( mIsPrintLog ) {
            log( finalTag + "\t" + ( msg.length() > MESSAGE_LENGTH_MAX ? msg.substring( MESSAGE_LENGTH_MAX ) : msg ) );
        }
    }


    public static void e( String tag, String msg, Throwable t ) {

        if( null == msg ) {
            msg = "";
        }
        String finalTag = TAG + tag;
        print( ERROR, finalTag, msg, t );
        if( mIsPrintLog ) {
            e( finalTag, t );
        }
    }


    public static void e( String tag, Throwable t ) {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter( sw, true );
        t.printStackTrace( pw );
        pw.flush();
        sw.flush();
        logAndCommit( tag + "\t" + sw.toString() );
    }


    /**
     * logAndCommit(这里用一句话描述这个方法的作用) (这里描述这个方法适用条件 – 可选)
     *
     * @param strLog
     *            void
     * @exception
     * @since 1.0.0
     */
    private static void logAndCommit( final String strLog ) {

        new Thread( new Runnable() {

            @Override
            public void run() {

                synchronized( mLock ) {
                    mLogArray.add( TimeUtil.getNowTimeString() + "\t" + strLog + "\r\n" );
                    commit();
                    mLogArray.clear();
                }
            }
        } ).start();

    }


    private static void commitLog() {

        if( mContext == null ) {
            return;
        }
        FileOutputStream logFile = null;
        try {
            boolean isSDCardExist = Environment.getExternalStorageState().equals( android.os.Environment.MEDIA_MOUNTED );
            if( isSDCardExist ) {
                String path = mContext.getExternalCacheDir().getAbsolutePath();
                if( null == path ) {
                    path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
                }
                File dir = new File( path );
                if( !dir.exists() ) {
                    if( !dir.mkdir() ) {
                        LogUtil.e(TAG, "make dir failed: " + dir);
                        return;
                    }
                }
                File file = new File( path + "/" + LOG_FILE_NAME );
                if( file.length() >= mMaxLogLength ) {
                    if( !file.delete() ) {
                        LogUtil.e(TAG, "delete file failed");
                        return;
                    }
                }
                // 创建一个文件
                if( !file.exists() ) {
                    file.createNewFile();
                }
                logFile = new FileOutputStream( file, true );
                for( String strItem : mLogArray ) {
                    logFile.write( strItem.getBytes() );
                }
            }
        } catch( Exception e ) {
            e.printStackTrace();
        } finally {
            if( logFile != null ) {
                try {
                    logFile.close();
                } catch( IOException e ) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }


    private static void print( int priority, String tag, String msg, Throwable tr ) {

        print( priority, tag, msg );
        print( priority, tag, getStackTraceString( tr ) );
    }


    private static void print( int priority, String tag, String msg ) {

        android.util.Log.println( priority, tag, msg );
    }


    private static String getStackTraceString( Throwable tr ) {

        if( tr == null ) {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter( sw );
        tr.printStackTrace( pw );
        return sw.toString();
    }


    private static final String LOG_PREFIX = "uamp_";
    private static final int LOG_PREFIX_LENGTH = LOG_PREFIX.length();
    private static final int MAX_LOG_TAG_LENGTH = 23;

    public static String makeLogTag(String str) {
        if (str.length() > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH) {
            return LOG_PREFIX + str.substring(0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1);
        }

        return LOG_PREFIX + str;
    }

    /**
     * Don't use this when obfuscating class names!
     */
    public static String makeLogTag(Class cls) {
        return makeLogTag(cls.getSimpleName());
    }


    public static void v(String tag, Object... messages) {
        // Only log VERBOSE if build type is DEBUG
        if (BuildConfig.DEBUG) {
            log(tag, Log.VERBOSE, null, messages);
        }
    }

    public static void d(String tag, Object... messages) {
        // Only log DEBUG if build type is DEBUG
        if (BuildConfig.DEBUG) {
            log(tag, Log.DEBUG, null, messages);
        }
    }

    public static void i(String tag, Object... messages) {
        log(tag, Log.INFO, null, messages);
    }

    public static void w(String tag, Object... messages) {
        log(tag, Log.WARN, null, messages);
    }

    public static void w(String tag, Throwable t, Object... messages) {
        log(tag, Log.WARN, t, messages);
    }

    public static void e(String tag, Object... messages) {
        log(tag, Log.ERROR, null, messages);
    }

    public static void e(String tag, Throwable t, Object... messages) {
        log(tag, Log.ERROR, t, messages);
    }

    public static void log(String tag, int level, Throwable t, Object... messages) {
        if (Log.isLoggable(tag, level)) {
            String message;
            if (t == null && messages != null && messages.length == 1) {
                // handle this common case without the extra cost of creating a stringbuffer:
                message = messages[0].toString();
            } else {
                StringBuilder sb = new StringBuilder();
                if (messages != null) for (Object m : messages) {
                    sb.append(m);
                }
                if (t != null) {
                    sb.append("\n").append(Log.getStackTraceString(t));
                }
                message = sb.toString();
            }
            Log.println(level, tag, message);
        }
    }


}
