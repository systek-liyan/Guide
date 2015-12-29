package com.systekcn.guide.biz;

import android.content.Context;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.exception.DbException;
import com.systekcn.guide.MyApplication;
import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.common.utils.ExceptionUtil;
import com.systekcn.guide.common.utils.LogUtil;
import com.systekcn.guide.common.utils.Tools;

import java.util.List;


/**
 * Created by Qiang on 2015/12/29.
 */
public class GetDataBiz implements IConstants {

    private IGetBeanBiz iGetBeanBiz;

    public void setIGetBeanBiz(IGetBeanBiz iGetBeanBiz) {
        this.iGetBeanBiz = iGetBeanBiz;
    }

    public List<?> getAllBeans(Context context,int type,String id){

        List<?> list = null;
        setIGetBeanBiz(new GetBeansFromLocal());
        list = iGetBeanBiz.getAllBeans(context,type,"",id);
        if (list==null||list.size()==0) {
            if (MyApplication.currentNetworkType != INTERNET_TYPE_NONE) {
                setIGetBeanBiz(new GetBeansFromNet());
                String url= Tools.checkTypeForNetUrl(type);
                list = iGetBeanBiz.getAllBeans(type, url,id);
            }
        }
        if(iGetBeanBiz instanceof GetBeansFromNet){
            boolean isSaveSuccess=saveAllBeans(context,list);
            LogUtil.i("ZHANG", "getAllBeans数据保存" + isSaveSuccess);
        }
        return list;
    }

    public  boolean saveAllBeans(Context context,List<?> list) {
        boolean isSuccess=false;
        if (list == null || list.size() == 0) {
            return isSuccess;
        }
        DbUtils db = DbUtils.create(context);
        try {
            db.saveAll(list);
            isSuccess=true;
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return isSuccess;
    }
}
