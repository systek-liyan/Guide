package com.systek.guide.download;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.systek.guide.IConstants;
import com.systek.guide.MyApplication;
import com.systek.guide.utils.ExceptionUtil;
import com.systek.guide.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Qiang on 2016/3/8.
 *
 * 下载任务控制
 */
public class TasksManagerDBController implements IConstants{


    public final static String TABLE_NAME = "tasksmanger";
    private final SQLiteDatabase db;

    public TasksManagerDBController() {
        TasksManagerDBOpenHelper openHelper = new TasksManagerDBOpenHelper(MyApplication.get());

        db = openHelper.getWritableDatabase();
    }

    /**
     * 获取所有的下载任务
     * @return
     */
    public List<TasksMuseumModel> getAllTasks() {
        final Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        final List<TasksMuseumModel> list = new ArrayList<>();
        try {
            while (c.moveToNext()) {
                TasksMuseumModel model = new TasksMuseumModel();
                model.setId(c.getInt(c.getColumnIndex(TasksMuseumModel.ID)));
                model.setName(c.getString(c.getColumnIndex(TasksMuseumModel.NAME)));
                model.setUrl(c.getString(c.getColumnIndex(TasksMuseumModel.URL)));
                model.setPath(c.getString(c.getColumnIndex(TasksMuseumModel.PATH)));
                model.setMuseumId(c.getString(c.getColumnIndex(TasksMuseumModel.MUSEUM_ID)));
                model.setIconUrl(c.getString(c.getColumnIndex(TasksMuseumModel.ICON_URL)));
                model.setProgress(c.getInt(c.getColumnIndex(TasksMuseumModel.PROGRESS)));
                model.setTotal(c.getInt(c.getColumnIndex(TasksMuseumModel.TOTAL)));
                list.add(model);
            }
        } catch (Exception e){
            ExceptionUtil.handleException(e);
        }finally {
            if (c != null) {
                c.close();
            }
        }

        return list;
    }

    /**
     * 获取所有的下载任务
     * @return
     */
    public TasksMuseumModel getSingleTask(int id) {
        final Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME+" WHERE 'id' ="+id, null);

        final List<TasksMuseumModel> list = new ArrayList<>();
        try {
            while (c.moveToNext()) {
                TasksMuseumModel model = new TasksMuseumModel();
                model.setId(c.getInt(c.getColumnIndex(TasksMuseumModel.ID)));
                model.setName(c.getString(c.getColumnIndex(TasksMuseumModel.NAME)));
                model.setUrl(c.getString(c.getColumnIndex(TasksMuseumModel.URL)));
                model.setPath(c.getString(c.getColumnIndex(TasksMuseumModel.PATH)));
                model.setMuseumId(c.getString(c.getColumnIndex(TasksMuseumModel.MUSEUM_ID)));
                model.setIconUrl(c.getString(c.getColumnIndex(TasksMuseumModel.ICON_URL)));
                model.setStatus(c.getInt(c.getColumnIndex(TasksMuseumModel.STATUS)));
                model.setProgress(c.getInt(c.getColumnIndex(TasksMuseumModel.PROGRESS)));
                model.setTotal(c.getInt(c.getColumnIndex(TasksMuseumModel.TOTAL)));
                list.add(model);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        if(list.size()==0){return null;}
        return list.get(0);
    }


    /**
     * 添加下载任务
     * @param task
     * @return
     */
    public TasksMuseumModel addTask(final TasksMuseumModel task) {
        if (task==null) {
            return null;
        }
        final boolean succeed = db.insert(TABLE_NAME, null, task.toContentValues()) != -1;
        return succeed ? task : null;

    }

    /**
     * 设置下载状态
     * @param id 下载id
     * @param state 状态
     */
    public void setStatus(int id,int state) {
        TasksMuseumModel task=  getSingleTask(id);
        if(task==null){return;} // TODO: 2016/3/9
        String museumId=task.getMuseumId();
        String sql="UPDATE "+TABLE_NAME+" SET "+TasksMuseumModel.STATUS+" = " +state+" WHERE id LIKE '"+museumId+"'";
        LogUtil.i("ZHANG","sql=="+sql);
        db.execSQL(sql);
    }

    /**
     * 设置下载进度
     * @param id 下载 id
     * @param progress 进度
     */
    public void setProgress(int id,int progress) {
        TasksMuseumModel task=  getSingleTask(id);
        if(task==null){return;} // TODO: 2016/3/9
        String museumId=task.getMuseumId();
        String sql="UPDATE "+TABLE_NAME+" SET "+TasksMuseumModel.PROGRESS+" = " +progress+" WHERE id LIKE '"+museumId+"'";
        LogUtil.i("ZHANG","sql=="+sql);
        db.execSQL(sql);
    }


    public void closeDB(){
        if(db!=null){
            db.close();
        }
    }

}
