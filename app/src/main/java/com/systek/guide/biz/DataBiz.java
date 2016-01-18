package com.systek.guide.biz;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.systek.guide.IConstants;
import com.systek.guide.MyApplication;
import com.systek.guide.entity.BeaconBean;
import com.systek.guide.entity.ExhibitBean;
import com.systek.guide.entity.LabelBean;
import com.systek.guide.utils.ExceptionUtil;
import com.systek.guide.utils.MyHttpUtil;

import org.altbeacon.beacon.Identifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Qiang on 2016/1/1.
 */
public class DataBiz implements IConstants{

    public static <T> List<T> getEntityListFromNet(Class<T> clazz,String url){

        String response= MyHttpUtil.sendGet(url);
        if(TextUtils.isEmpty(response)){return null;}
        List<T> list=JSON.parseArray(response,clazz);
        return list;
    }
    public synchronized static<T> List<T> getEntityListLocal(Class<T> clazz){
        DbUtils db=DbUtils.create(MyApplication.get());
        List<T> list= null;
        try {
            list = db.findAll(clazz);
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }finally {
            if(db!=null){db.close();}
        }
        return list;
    }

    public synchronized static<T> List<T> getEntityListLocalByColumn(String column,String value,Class<T> clazz){
        DbUtils db=DbUtils.create(MyApplication.get());
        List<T> list= null;
        try {
            list = db.findAll(Selector.from(clazz).where(column,"=",value));
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }finally {
            if(db!=null){db.close();}
        }
        return list;
    }

    public synchronized static <T> boolean deleteSQLiteDataFromClass(Class<T> clazz){
        boolean isSuccess=true;
        DbUtils db=null;
        try {
            db=DbUtils.create(MyApplication.get());
            db.deleteAll(clazz);
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
            isSuccess=false;
        }finally {
            if(db!=null){db.close();}
        }
        return isSuccess;
    }

    public synchronized static <T> boolean deleteSQLiteDataFromID(Class<T> clazz,String id){
        boolean isSuccess=true;
        DbUtils db=DbUtils.create(MyApplication.get());
        try {
            db.delete(clazz, WhereBuilder.b(ID, LIKE, "%" + id + "%"));
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
            isSuccess=false;
        }finally {
            if(db!=null){db.close();}
        }
        return isSuccess;
    }



    public synchronized static<T>  boolean saveListToSQLite(List<T> list){
        boolean isSuccess=true;
        if(list==null){return false; }
        DbUtils db=null;
        try {
            db=DbUtils.create(MyApplication.get());
            db.saveOrUpdateAll(list);
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
            isSuccess=false;
        }finally {
            if(db!=null){db.close();}
        }
        return isSuccess;
    }

    public synchronized static boolean saveEntityToSQLite(Object obj){
        boolean isSuccess=true;
        DbUtils db=DbUtils.create(MyApplication.get());
        try {
            db.save(obj);
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
            isSuccess=false;
        }finally {
            if(db!=null){db.close();}
        }
        return isSuccess;
    }

    public synchronized static boolean deleteOldJsonData(String museumID){
        boolean isSuccess=true;
        DbUtils db=DbUtils.create(MyApplication.get());
        try{
            db.createTableIfNotExist(BeaconBean.class);
            List<BeaconBean> beaconList=db.findAll(Selector.from(BeaconBean.class).where(MUSEUM_ID, LIKE, "%" + museumID + "%"));
            if(beaconList!=null&&beaconList.size()>0){
                db.deleteAll(beaconList);
            }
            db.createTableIfNotExist(LabelBean.class);
            List<LabelBean> labelList=db.findAll(Selector.from(LabelBean.class).where(MUSEUM_ID, LIKE, "%" + museumID + "%"));
            if(labelList!=null&&labelList.size()>0){
                db.deleteAll(labelList);
            }
            db.createTableIfNotExist(ExhibitBean.class);
            List<ExhibitBean> exhibitBeanList=db.findAll(Selector.from(ExhibitBean.class).where(MUSEUM_ID, LIKE, "%" + museumID + "%"));
            if(exhibitBeanList!=null&&exhibitBeanList.size()>0){
                db.deleteAll(exhibitBeanList);
            }
        }catch (DbException e){
            isSuccess=false;
            ExceptionUtil.handleException(e);
        }finally {
            if(db!=null){
                db.close();
            }
        }
        return isSuccess;
    }



    public synchronized static List<ExhibitBean> getCollectionExhibitListFromDB() {
        List<ExhibitBean> collectionList=null;
        DbUtils db=null;
        try {
            db=DbUtils.create(MyApplication.get());
            collectionList= db.findAll(Selector.from(ExhibitBean.class).where(SAVE_FOR_PERSON,"=", true));
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }finally {
            if(db!=null){
                db.close();
            }
        }
        return collectionList;
    }
    public synchronized static List<ExhibitBean> getCollectionExhibitListFromDBById(String museumId) {
        List<ExhibitBean> collectionList=null;
        DbUtils db=DbUtils.create(MyApplication.get());
        try {
            collectionList= db.findAll(Selector.from(ExhibitBean.class).where(SAVE_FOR_PERSON, "=", true).and(MUSEUM_ID, LIKE, "%" + museumId + "%"));
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }finally {
            if(db!=null){
                db.close();
            }
        }
        return collectionList;
    }

    public synchronized static boolean saveAllJsonData(String museumID) {
        List<BeaconBean> beaconList = getEntityListFromNet(BeaconBean.class, URL_BEACON_LIST + museumID);
        List<LabelBean> labelList = getEntityListFromNet(LabelBean.class, URL_LABELS_LIST + museumID);
        List<ExhibitBean> exhibitList = getEntityListFromNet(ExhibitBean.class, URL_EXHIBIT_LIST + museumID);
        if(beaconList == null || labelList == null || exhibitList == null //|| mapList == null//|| mapList.size() == 0
                || beaconList.size() == 0 || labelList.size() == 0 || exhibitList.size() == 0 ){return false;}
        List<ExhibitBean> collectionList= getCollectionExhibitListFromDBById(museumID);
        if(collectionList!=null&&collectionList.size()>0){
            exhibitList.removeAll(collectionList);
        }
        return saveListToSQLite(beaconList) && saveListToSQLite(labelList) && saveListToSQLite(exhibitList);// && saveEntityToSQLite(mapList)
    }


    public synchronized static<T>  List<T> getLocalListById(Class<T> clazz, String museumID) {
        DbUtils db=DbUtils.create(MyApplication.get());
        List<T>list=null;
        try {
            list =db.findAll(Selector.from(clazz).where(MUSEUM_ID,LIKE,"%"+museumID+"%"));
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }finally {
            if(db!=null){
                db.close();
            }
        }
        return list;
    }

    public synchronized static void saveOrUpdate(Object obj) {
        DbUtils db=DbUtils.create(MyApplication.get());
        try {
            db.saveOrUpdate(obj);
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }finally {
            if(db!=null){
                db.close();
            }
        }
    }


    public synchronized static List<ExhibitBean> getExhibitListByBeaconId(String museumId,String beaconId){

        if(TextUtils.isEmpty(beaconId)){return null;}
        DbUtils db=null;
        List<ExhibitBean> list=null;
        try {
            db=DbUtils.create(MyApplication.get());
            list=db.findAll(Selector.from(ExhibitBean.class).where(BEACON_ID,LIKE,"%"+beaconId+"%"));
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
        }
        if(list!=null){return list;}
        String url=URL_EXHIBIT_LIST+museumId+"&beaconId="+beaconId;
        String response=MyHttpUtil.sendGet(url);
        if(TextUtils.isEmpty(response)){return null;}
        list=JSON.parseArray(response,ExhibitBean.class);
        return list;
    }

    /**
     * 向SP文件存储数据
     * @param context
     * @param key  键名
     * @param value  键值
     */
    public static void saveTempValue(Context context, String key, Object value) {
        try {
            SharedPreferences sp = context.getApplicationContext().getSharedPreferences("temp", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            if (value instanceof Integer) {
                editor.putInt(key, (Integer) value);
            } else if (value instanceof String) {
                editor.putString(key, (String) value);
            } else if (value instanceof Boolean) {
                editor.putBoolean(key, (Boolean) value);
            }
            editor.apply();
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
        }
    }


    public static  String getCurrentMuseumId(){
       return (String) getTempValue(MyApplication.get(),SP_MUSEUM_ID,"");
    }
    /**
     * 从SP文件中读取指定Key的值
     * type=1/数值 defValue=-1 | type=2/字符串 defValue=null | type=3/布尔
     * defValue=false
     * @param context
     * @param key  键名
     * @return 键值
     */
    public static Object getTempValue(Context context, String key, Object defaultObject) {
        try {
            SharedPreferences sp = context.getApplicationContext().getSharedPreferences("temp", Context.MODE_PRIVATE);
            if (defaultObject instanceof Integer) {
                return sp.getInt(key, (Integer) defaultObject);
            } else if (defaultObject instanceof String) {
                return sp.getString(key, (String) defaultObject);
            } else if (defaultObject instanceof Boolean) {
                return sp.getBoolean(key, (Boolean) defaultObject);
            }
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
        }
        return null;
    }

    /**
     * 清空
     * @param context
     */
    public static void clearTempValues(Context context) {
        try {
            SharedPreferences sp = context.getSharedPreferences("temp", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.clear();
            editor.apply();
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
        }
    }

    /** 保存方法 */
    public static boolean saveBitmap(String path,String name,Bitmap bm) {
        boolean isSave=false;
        File f = new File(path,name);
        if (!f.exists()) {
            f.delete();
        }
        FileOutputStream out=null;
        try {
            out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            isSave=true;
        } catch (IOException e) {
            ExceptionUtil.handleException(e);
        }finally {
            if(out!=null){
                try {
                    out.close();
                } catch (IOException e) {
                    ExceptionUtil.handleException(e);
                }
            }
        }
        return isSave;
    }

    /**
     * 通过minor和major获取beacon
     * @param minor beacon属性
     * @param major beacon属性
     * @return beacon对象
     */
    public synchronized static BeaconBean getBeaconMinorAndMajor(Identifier minor,Identifier major){
        List<BeaconBean> beaconBeans=null;
        BeaconBean b=null;
        DbUtils db=null;
        try {
            db=DbUtils.create(MyApplication.get());
            beaconBeans= db.findAll(Selector.from(BeaconBean.class).where("minor", "=", minor).and("major","=",major));
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
        }finally {
            if(db!=null){
                db.close();
            }
        }
        /*try{
            if(beaconBeans==null||beaconBeans.size()<=0){
                String url=URL_ALL_BEACON_LIST + "?minor=" + minor + "&major=" + major;
                String response= MyHttpUtil.sendGet(url);
                if(TextUtils.isEmpty(response)||response.equals("[]")){return null;}
                beaconBeans=JSON.parseArray(response, BeaconBean.class);// TODO: 2016/1/15
                if(beaconBeans==null||beaconBeans.size()==0){return null;}
                b=beaconBeans.get(0);
            }else{
                b=beaconBeans.get(0);
            }
        }catch (Exception e){
            ExceptionUtil.handleException(e);
            return null;
        }*/
        if(beaconBeans==null||beaconBeans.size()<=0){return null;}
        return beaconBeans.get(0);
        //return b;
    }

    public synchronized static List<ExhibitBean> getExhibitListByLabel(String label){
        List<ExhibitBean> list = null;
        DbUtils db=null;
        try {
            db=DbUtils.create(MyApplication.get());
            list=  db.findAll(Selector.from(ExhibitBean.class).where("labels","like","%"+label+"%"));
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }finally {
            if(db!=null){
                db.close();
            }
        }
        return list;
    }

}
