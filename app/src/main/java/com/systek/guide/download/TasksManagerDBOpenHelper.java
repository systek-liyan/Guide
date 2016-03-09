package com.systek.guide.download;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Qiang on 2016/3/8.
 *
 * 数据库操作类
 */
public class TasksManagerDBOpenHelper   extends SQLiteOpenHelper {

    public final static String DATABASE_NAME = "tasksmanager.db";
    public final static int DATABASE_VERSION = 1;

    public TasksManagerDBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS "
                + TasksManagerDBController.TABLE_NAME
                + String.format(
                "("
                        + "%s INTEGER PRIMARY KEY, " // id, download id
                        + "%s VARCHAR, " // name
                        + "%s VARCHAR, " // url
                        + "%s VARCHAR, " // path
                        + "%s VARCHAR, " // museumId
                        + "%s VARCHAR, " // iconUrl
                        + "%s INTEGER, " // status
                        + "%s INTEGER, " // progress
                        + "%s INTEGER  " // total
                        + ")"
                , TasksMuseumModel.ID
                , TasksMuseumModel.NAME
                , TasksMuseumModel.URL
                , TasksMuseumModel.PATH
                , TasksMuseumModel.MUSEUM_ID
                , TasksMuseumModel.ICON_URL
                , TasksMuseumModel.STATUS
                , TasksMuseumModel.PROGRESS
                , TasksMuseumModel.TOTAL

        ));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
