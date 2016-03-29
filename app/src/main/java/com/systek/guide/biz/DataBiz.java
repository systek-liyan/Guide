package com.systek.guide.biz;


import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import com.systek.guide.entity.base.BaseEntity;
import com.systek.guide.utils.ExceptionUtil;
import com.systek.guide.utils.MyHttpUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Qiang on 2016/1/1.
 *
 *
 */
public class DataBiz implements IConstants{


    private static DbUtils db = null;
    private static final String DATABASE_NAME = "guide.db";
    private static final int DATABASE_VERSION = 1;

    public static  DbUtils getDb() {
        if (db == null) {
            synchronized (DownloadManager.class) {
                if(db == null){
                    Context context = MyApplication.get();
                    db = DbUtils.create(context, DATABASE_NAME,DATABASE_VERSION, new DbUtils.DbUpgradeListener() {
                        @Override
                        public void onUpgrade(DbUtils db, int oldversion, int newVersion) {

                            if (newVersion > oldversion) {

                                updateDb(db, "Exhibit");
                            }
                        }
                    });
                }
            }
        }
        db.configAllowTransaction(true);
        return db;
    }

    public static void closeDB(){
        /*if(db!=null){
            db.close();
        }*/
    }



    private static void updateDb(DbUtils db, String tableName) {

        try {

            Class<BaseEntity> c = (Class<BaseEntity>) Class.forName("com.sysytek.guide.entity." + tableName);// 把要使用的类加载到内存中,并且把有关这个类的所有信息都存放到对象c中

            if (db.tableIsExist(c)) {

                List<String> dbFildsList = new ArrayList<>();

                String str = "select * from " + tableName;

                Cursor cursor = db.execQuery(str);

                int count = cursor.getColumnCount();

                for (int i = 0; i < count; i++) {

                    dbFildsList.add(cursor.getColumnName(i));

                }

                cursor.close();

                Field f[] = c.getDeclaredFields();// 把属性的信息提取出来，并且存放到field类的对象中，因为每个field的对象只能存放一个属性的信息所以要用数组去接收

                for (int i = 0; i < f.length; i++) {

                    String fildName = f[i].getName();

                    if (fildName.equals("serialVersionUID")) {

                        continue;

                    }

                    String fildType = f[i].getType().toString();

                    if (!isExist(dbFildsList, fildName)) {

                        if (fildType.equals("class Java.lang.String")) {

                            db.execNonQuery("alter table " + tableName + " add " + fildName + " TEXT ");

                        } else if (fildType.equals("int") || fildType.equals("long") || fildType.equals("boolean")) {

                            db.execNonQuery("alter table " + tableName + " add " + fildName + " INTEGER ");

                        }

                    }

                }

            }

        } catch (Exception e) {
            ExceptionUtil.handleException(e);
        }

    }

    /**
     * 判断List中是否存在某元素
     *
     * @param dbFildsList 集合
     * @param fildName 元素
     * @return 是否存在
     */
    private static boolean isExist(List<String>  dbFildsList, String fildName) {
        return !(dbFildsList == null || fildName == null || dbFildsList.size() == 0 || fildName.equals("")) && dbFildsList.contains(fildName);
    }


    /**
     * 根据实体类，url获取对象集合
     * @param clazz 实体类
     * @param url url
     * @param <T>
     * @return
     */
    public synchronized static <T> List<T> getEntityListFromNet(Class<T> clazz,String url){
        String response= MyHttpUtil.doGet(url);
        if(TextUtils.isEmpty(response)){return null;}
        return JSON.parseArray(response, clazz);
    }

    /**
     * 根据实体类从本地数据数据库查询实体类集合
     * @param clazz
     * @param <T>
     * @return
     */
    public synchronized static<T> List<T> getEntityListLocal(Class<T> clazz){
        List<T> list= null;
        try {
            list = getDb().findAll(clazz);
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }finally {
            closeDB();
        }
        return list;
    }

    public synchronized static List<ExhibitBean> searchFromSQLite(String museumId,String s) {
        List<ExhibitBean> list=null;
        try {
            //list= db.findAll(Selector.from(ExhibitBean.class).where(LABELS,LIKE,"%"+s+"%").or(NAME,LIKE,"%"+s+"%"));
            list= getDb().findAll(Selector.from(ExhibitBean.class).where(MUSEUM_ID,LIKE,"%"+museumId+"%").and(LABELS,LIKE,"%"+s+"%").or(NAME,LIKE,"%"+s+"%"));
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
        }
        return list;
    }



    /**
     * 从本地数据数据库查询实体类
     * @param clazz 类
     * @param <T> 类型
     * @return 对象
     */
    public synchronized static<T> T getEntityLocalById(Class<T> clazz,String id){
        T t=null;
        try {
            t = getDb().findById(clazz,id);
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }finally {
            closeDB();
        }
        return t;
    }



    /**
     * 查询数据库下某column列下值为value的集合
     * @param column
     * @param value
     * @param clazz
     * @param <T>
     * @return
     */
    public synchronized static<T> List<T> getEntityListLocalByColumn(String column,String value,Class<T> clazz){
        List<T> list= null;
        try {
            list = getDb().findAll(Selector.from(clazz).where(column, "=", value));
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }finally {
           closeDB();
        }
        return list;
    }

    /**
     * 删除数据库内clazz类的所有数据
     * @param clazz
     * @param <T>
     * @return
     */
    public synchronized static <T> boolean deleteSQLiteDataFromClass(Class<T> clazz){
        boolean isSuccess=true;
        try {
            getDb().deleteAll(clazz);
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
            isSuccess=false;
        }finally {
            closeDB();
        }
        return isSuccess;
    }

    /**
     * 删除数据库内class类id值为id的数据
     * @param clazz
     * @param id
     * @param <T>
     * @return
     */
    public synchronized static <T> boolean deleteSQLiteDataFromID(Class<T> clazz,String id){
        boolean isSuccess=true;
        try {
            getDb().delete(clazz, WhereBuilder.b(ID, LIKE, "%" + id + "%"));
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
            isSuccess=false;
        }finally {
            closeDB();
        }
        return isSuccess;
    }

    /**
     * 保存或更新数据集合
     * @param list
     * @param <T>
     * @return
     */
    public synchronized static<T>  boolean saveListToSQLite(List<T> list){
        boolean isSuccess=true;
        if(list==null){return false; }
        try {
            getDb().saveOrUpdateAll(list);
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
            isSuccess=false;
        }finally {
            closeDB();
        }
        return isSuccess;
    }

    /**
     * 保存对象至数据库
     * @param obj
     * @return
     */
    public synchronized static boolean saveEntityToSQLite(Object obj){
        boolean isSuccess=true;
        try {
            getDb().save(obj);
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
            isSuccess=false;
        }finally {
            closeDB();
        }
        return isSuccess;
    }

    /**
     * 删除旧的博物馆数据
     * @param museumID 博物馆id
     * @return 是否删除成功
     */
    public synchronized static boolean deleteOldJsonData(String museumID){
        boolean isSuccess=true;
        DbUtils db=getDb();
        //DbUtils db=DbUtils.create(MyApplication.get());
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
            closeDB();
        }
        return isSuccess;
    }


    /**
     * 获取收藏展品集合
     * @return
     */
    public synchronized static List<ExhibitBean> getCollectionExhibitListFromDB() {
        List<ExhibitBean> collectionList=null;
        //DbUtils db=DbUtils.create(MyApplication.get());
        try {
            collectionList= getDb().findAll(Selector.from(ExhibitBean.class).where(SAVE_FOR_PERSON, "=", true));
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }finally {
            closeDB();
        }
        return collectionList;
    }

    /**
     * 获取博物馆id值为 museumId 的展品结合
     * @param museumId
     * @return
     */
    public synchronized static List<ExhibitBean> getCollectionExhibitListFromDBById(String museumId) {
        List<ExhibitBean> collectionList=null;
        //DbUtils db=DbUtils.create(MyApplication.get());
        try {
            collectionList= getDb().findAll(Selector.from(ExhibitBean.class).where(SAVE_FOR_PERSON, "=", true).and(MUSEUM_ID, LIKE, "%" + museumId + "%"));
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }finally {
            closeDB();
        }
        return collectionList;
    }

    public synchronized static ExhibitBean getExhibitFromDBById(String exhibitId) {
        ExhibitBean exhibitBean=null;
        //DbUtils db=DbUtils.create(MyApplication.get());
        try {
            exhibitBean= getDb().findById(ExhibitBean.class, exhibitId);
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }finally {
            closeDB();
        }
        return exhibitBean;
    }

    /**
     * 展品等博物馆数据是否保存
     * @param museumd 博物馆id
     * @return 是否有数据
     */
    public synchronized static boolean isBasicDataSave(String museumd) {
        List<BeaconBean> beaconList = getEntityListLocal(BeaconBean.class);
        List<LabelBean> labelList = getEntityListLocal(LabelBean.class);
        List<ExhibitBean> exhibitList = getEntityListLocal(ExhibitBean.class);
        return beaconList != null && labelList != null && exhibitList != null
                && beaconList.size() > 0 && labelList.size() > 0 && exhibitList.size() > 0;
    }

    /**
     * 保存博物馆下展品，beacon，label数据
     * @param museumID
     * @return
     */
    public synchronized static boolean saveAllJsonData(String museumID) {
        String beaconUrl=BASE_URL+URL_BEACON_LIST + museumID;
        List<BeaconBean> beaconList = getEntityListFromNet(BeaconBean.class, beaconUrl);
        String labelUrl=BASE_URL+URL_LABELS_LIST + museumID;
        List<LabelBean> labelList = getEntityListFromNet(LabelBean.class, labelUrl);
        String exhibitUrl=BASE_URL+URL_EXHIBIT_LIST + museumID;
        List<ExhibitBean> exhibitList = getEntityListFromNet(ExhibitBean.class, exhibitUrl);
        if(beaconList == null || labelList == null || exhibitList == null //|| mapList == null//|| mapList.size() == 0
                || beaconList.size() == 0 || labelList.size() == 0 || exhibitList.size() == 0 ){return false;}

        List<ExhibitBean> collectionList= getCollectionExhibitListFromDBById(museumID);
        if(collectionList!=null&&collectionList.size()>0){
            exhibitList.removeAll(collectionList);
        }
        return saveListToSQLite(beaconList) && saveListToSQLite(labelList) && saveListToSQLite(exhibitList);// && saveEntityToSQLite(mapList)
    }

    /**
     * 获取 博物馆id值为museumid的 class类数据集合
     * @param clazz
     * @param museumID
     * @param <T>
     * @return
     */
    public synchronized static<T>   List<T> getLocalListById(Class<T> clazz, String museumID) {
        //DbUtils db=getDb();
        //DbUtils db=DbUtils.create(MyApplication.get());
        List<T>list=null;
        try {
            list =getDb().findAll(Selector.from(clazz).where(MUSEUM_ID, LIKE, "%" + museumID +"%"));
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }finally {
            closeDB();
        }
        return list;
    }

    /**
     * 保存或更新对象数据至数据库
     * @param obj
     */
    public synchronized static  void saveOrUpdate(Object obj) {
        //DbUtils db=getDb();
        //DbUtils db=DbUtils.create(MyApplication.get());
        try {
            getDb().saveOrUpdate(obj);
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }finally {
            closeDB();
        }
    }

    /**
     * 通过beacon id 获取展品集合
     * @param museumId
     * @param beaconId
     * @return
     */
    public synchronized static List<ExhibitBean> getExhibitListByBeaconId(String museumId,String beaconId){

        if(TextUtils.isEmpty(beaconId)){return null;}
        //DbUtils db=getDb();
        //DbUtils db=DbUtils.create(MyApplication.get());
        List<ExhibitBean> list=null;
        try {
            list=getDb().findAll(Selector.from(ExhibitBean.class).where(BEACON_ID, LIKE, "%" + beaconId +"%"));
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
        }finally {
            closeDB();
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
    public synchronized static  void saveTempValue(Context context, String key, Object value) {
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
        return (String) getTempValue(MyApplication.get(),SP_MUSEUM_ID,"deadccf89ef8412a9c8a2628cee28e18");
    }
    /**
     * 从SP文件中读取指定Key的值
     * type=1/数值 defValue=-1 | type=2/字符串 defValue=null | type=3/布尔
     * defValue=false
     * @param context
     * @param key  键名
     * @return 键值
     */
    public synchronized static  Object getTempValue(Context context, String key, Object defaultObject) {
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
        if (f.exists()) {
            return true;
            //f.delete();
        }
        FileOutputStream out=null;
        try {
            out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            isSave=true;
            //LogUtil.i("ZHANG","图片保存成功=name="+name+"path="+path);
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
    public  static synchronized BeaconBean getBeaconMinorAndMajor(String minor,String major){
        List<BeaconBean> beaconBeans=null;
        BeaconBean b=null;
        try {
            beaconBeans= getDb().findAll(Selector.from(BeaconBean.class).where("minor", "=", minor).and("major", "=", major));
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
        }finally {
            closeDB();
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
        //return b;
        if(beaconBeans==null||beaconBeans.size()<=0){return null;}
        return beaconBeans.get(0);
    }

    /**
     * 通过展品label获取展品集合
     * @param label
     * @return
     */
    public synchronized static  List<ExhibitBean> getExhibitListByLabel(String label){// TODO: 2016/1/21 应加入博物馆id
        List<ExhibitBean> list = null;
        try {
            list=  getDb().findAll(Selector.from(ExhibitBean.class).where("labels", "like", "%" + label +"%"));
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }finally {
           closeDB();
        }
        return list;
    }

}
