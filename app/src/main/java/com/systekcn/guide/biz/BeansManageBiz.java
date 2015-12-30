package com.systekcn.guide.biz;


import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.systekcn.guide.MyApplication;
import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.common.utils.ExceptionUtil;
import com.systekcn.guide.common.utils.LogUtil;
import com.systekcn.guide.common.utils.MyHttpUtil;
import com.systekcn.guide.common.utils.Tools;
import com.systekcn.guide.entity.BeaconBean;
import com.systekcn.guide.entity.ExhibitBean;

import org.altbeacon.beacon.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Qiang on 2015/10/22.
 */
public class BeansManageBiz implements IConstants{

    Context context;
    IGetBeanBiz iGetBeanBiz;

    BeansManageBiz(Context context) {
        this.context = context;
    }

    public void setIGetBeanBiz(IGetBeanBiz iGetBeanBiz) {
        this.iGetBeanBiz = iGetBeanBiz;
    }

    public <T> List<T> getAllBeans(int type,Class<T> clazz,String id) {
        List<T> list = null;
        setIGetBeanBiz(new GetBeansFromLocal());
        list = iGetBeanBiz.getAllBeans(context,type,"",id);

        if (list==null) {
            if (MyApplication.currentNetworkType != INTERNET_TYPE_NONE) {
                setIGetBeanBiz(new GetBeansFromNet());
                String url= Tools.checkTypeForNetUrl(type);
                list = iGetBeanBiz.getAllBeans(context,type, url,id);
            }else{
                list=new ArrayList<T>();
                return list;
            }
        }
        if(iGetBeanBiz instanceof GetBeansFromNet){
            boolean isSaveSuccess=saveAllBeans(list);
            LogUtil.i("ZHANG", "数据保存" + isSaveSuccess);
        }
        return (List<T>) list;
    }

    public <T> List<T> getAllBeansByNet(int type,Class<T> clazz,String id) {
        List<T> list = null;
        if (MyApplication.currentNetworkType != INTERNET_TYPE_NONE) {
            setIGetBeanBiz(new GetBeansFromNet());
            String url= Tools.checkTypeForNetUrl(type);
            list = iGetBeanBiz.getAllBeans(context,type, url,id);
        }else{
            list=new ArrayList<T>();
            return list;
        }
        boolean isSaveSuccess = saveAllBeans(list);
        LogUtil.i("ZHANG", "数据保存" + isSaveSuccess);
        return (List<T>) list;
    }



    public Object getBeanById(int type, String id) {

        Object object = null;
        setIGetBeanBiz(new GetBeansFromLocal());
        object = iGetBeanBiz.getBeanById(context,type,"",id);
        if (object==null) if (MyApplication.currentNetworkType != INTERNET_TYPE_NONE) {
            setIGetBeanBiz(new GetBeansFromNet());
            String url = Tools.checkTypeForNetUrl(type);
            object = iGetBeanBiz.getBeanById(context, type, url, id);
        } else {
            return null;
        }
        if (iGetBeanBiz instanceof GetBeansFromNet) {
            boolean isSaveSuccess = saveBean(object);
            LogUtil.i("ZHANG", "数据保存" + isSaveSuccess);
        }
        return object;
    }


    List<BeaconBean> beaconBeans=null;

    public BeaconBean getBeaconMinorAndMajor(Identifier minor,Identifier major){
        BeaconBean b=null;
        if(context==null){
            context=MyApplication.get().getApplicationContext();
        }
        DbUtils db=DbUtils.create(context);
        try {
            beaconBeans= db.findAll(Selector.from(BeaconBean.class).where("minor", "=", minor).and("major","=",major));
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }finally {
            if(db!=null){
                db.close();
            }
        }
        if(beaconBeans==null||beaconBeans.size()<=0){
            String url=URL_ALL_BEACON_LIST + "?minor=" + minor + "&major=" + major;
            String response=MyHttpUtil.get(url);
            beaconBeans=JSON.parseArray(response, BeaconBean.class);
            b=beaconBeans.get(0);
        }else{
            b=beaconBeans.get(0);
        }
        return b;
    }

    List<ExhibitBean> list =null;
    boolean isGetExhibitsOver=false;
    public List<ExhibitBean> getExhibitListByBeaconId(String museumId,String beaconId){

        DbUtils db=DbUtils.create(context);
        HttpUtils http= new HttpUtils();
        if(beaconId==null){
            return null;
        }else{
            try {
                list=db.findAll(Selector.from(ExhibitBean.class).where("beaconId","like","%"+beaconId+"%"));
            } catch (DbException e) {
                ExceptionUtil.handleException(e);
            }
            isGetExhibitsOver=true;
            if(list==null){
                isGetExhibitsOver=false;
                String url="http://182.92.82.70/api/exhibitService/exhibitList?"+"museumId="+museumId+"&"+"beaconId="+beaconId;
                http.send(HttpRequest.HttpMethod.GET, url, new RequestCallBack<String>() {

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        list=JSON.parseArray(responseInfo.result, ExhibitBean.class);
                        isGetExhibitsOver=true;
                    }
                    @Override
                    public void onFailure(HttpException error, String msg) {
                    }
                });
            }
            while(!isGetExhibitsOver){}
            return (List<ExhibitBean>) list;
        }
    }


    private <T>boolean saveBean(T t) {
        if (t == null ) {
            return false;
        }
        DbUtils db = DbUtils.create(context);
        try {
            db.save(t);
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return true;
    }

    public <T> boolean saveAllBeans(List<?> list) {
        if (list == null || list.size() == 0) {
            return false;
        }
        DbUtils db = DbUtils.create(context);
        try {
            db.saveAll(list);
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return true;
    }


}
