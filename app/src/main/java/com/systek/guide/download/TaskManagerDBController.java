package com.systek.guide.download;


import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.exception.DbException;
import com.systek.guide.IConstants;
import com.systek.guide.MyApplication;
import com.systek.guide.utils.ExceptionUtil;

import java.util.List;

/**
 * Created by Qiang on 2016/3/8.
 *
 * 下载任务控制
 */
public class TaskManagerDBController implements IConstants{

    DbUtils db;

    private synchronized DbUtils getDb(){
        db=DbUtils.create(MyApplication.get());
        db.configAllowTransaction(true);
        return db;
    }

    public TaskManagerDBController() {

    }

    /**
     * 获取所有的下载任务
     * @return
     */
    public List<TasksMuseumModel> getAllTasks() {
        List<TasksMuseumModel> list=null;
        try {
            list=getDb().findAll(TasksMuseumModel.class);
        } catch (DbException e) {
           ExceptionUtil.handleException(e);
        }finally {
            closeDB();
        }
        return list;
    }

    /**
     * 获取所有的下载任务
     * @return
     */
    public synchronized TasksMuseumModel getSingleTask(int id) {

        TasksMuseumModel tasksMuseumModel=null;
        try {
            tasksMuseumModel= getDb().findById(TasksMuseumModel.class,id);
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        } finally {
                closeDB();
        }
        return tasksMuseumModel;
    }


    /**
     * 添加下载任务
     * @param task
     * @return
     */
    public TasksMuseumModel addTask(final TasksMuseumModel task) {
        boolean flag=false;
        if (task==null) {
            return null;
        }
        try {
            getDb().save(task);
            flag=true;
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }finally {
            closeDB();
        }
        return flag?task:null;

    }

    /**
     * 设置下载状态
     * @param id 下载id
     * @param state 状态
     */
    public void setStatus(int id,int state) {
        TasksMuseumModel task=  getSingleTask(id);
        if(task==null){return;} // TODO: 2016/3/9
        task.setStatus(state);
        try {
            getDb().save(task);
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }finally {
            closeDB();
        }
    }

    /**
     * 设置下载进度
     * @param id 下载 id
     * @param progress 进度
     */
    public void setProgress(int id,float progress) {
        TasksMuseumModel task=  getSingleTask(id);
        if(task==null){return;} // TODO: 2016/3/9
        task.setProgress(progress);
        try {
            getDb().saveOrUpdate(task);
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }finally {
            closeDB();
        }
    }


    public void closeDB(){
        if(db!=null){
            db.close();
        }
    }

}
