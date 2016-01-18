package com.systek.guide.biz;

import android.content.Context;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.exception.DbException;
import com.systek.guide.utils.ExceptionUtil;
import com.systek.guide.utils.Tools;

import java.util.List;

/**
 * Created by Qiang on 2015/10/22.
 */
public class GetBeansFromLocal implements IGetBeanBiz {



    @Override
    public <T> List<T> getAllBeans(Context context, int type, String url, String id) {
        Class clazz= Tools.checkTypeForClass(type);
        DbUtils db=DbUtils.create(context);
        List<T> list=null;
        try {
            list = db.findAll(clazz);
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }finally{
            if(db!=null){
                db.close();
            }
        }
        return list;
    }

    @Override
    public <T> List<T> getAllBeans(int type, String url, String Id) {
        return null;
    }

    @Override
    public <T> T getBeanById(Context context, int type, String url, String id) {
        Class clazz= Tools.checkTypeForClass(type);
        DbUtils db=DbUtils.create(context);
        T t=null;
        try {
            t= (T) db.findById(clazz, id);
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }finally{
            if(db!=null){
                db.close();
            }
        }
        return t;
    }
}
