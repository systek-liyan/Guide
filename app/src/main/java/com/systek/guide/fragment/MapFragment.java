package com.systek.guide.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.ls.widgets.map.MapWidget;
import com.ls.widgets.map.config.GPSConfig;
import com.ls.widgets.map.config.OfflineMapConfig;
import com.ls.widgets.map.events.MapScrolledEvent;
import com.ls.widgets.map.events.MapTouchedEvent;
import com.ls.widgets.map.events.ObjectTouchEvent;
import com.ls.widgets.map.interfaces.Layer;
import com.ls.widgets.map.interfaces.MapEventsListener;
import com.ls.widgets.map.interfaces.OnLocationChangedListener;
import com.ls.widgets.map.interfaces.OnMapScrollListener;
import com.ls.widgets.map.interfaces.OnMapTouchListener;
import com.ls.widgets.map.model.MapObject;
import com.ls.widgets.map.utils.PivotFactory;
import com.systek.guide.IConstants;
import com.systek.guide.R;
import com.systek.guide.biz.DataBiz;
import com.systek.guide.biz.map.MapObjectContainer;
import com.systek.guide.biz.map.MapObjectModel;
import com.systek.guide.biz.map.TextPopup;
import com.systek.guide.entity.BeaconBean;
import com.systek.guide.entity.ExhibitBean;
import com.systek.guide.manager.BluetoothManager;
import com.systek.guide.manager.MediaServiceManager;
import com.systek.guide.utils.ExceptionUtil;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends BaseFragment implements IConstants, MapEventsListener, OnMapTouchListener {


    private static final String TAG = "MapFragment";
    private static final Integer PERSON_LAYER = 0;//人员定位图层
    private static final Integer EXHIBITS_LAYER = 1;//展品显示图层
    private static final int MAP_ID = 23;
    private int nextObjectId;
    private int pinHeight;
    private MapObjectContainer model;
    private MapWidget map;
    private TextPopup mapObjectInfoPopup;
    private Activity activity;
    private Location points[];
    private int currentPoint;
    private BluetoothManager bluetoothManager;
    private BeaconBean beacon;
    private static MapFragment mapFragment;
    private List<ExhibitBean> topicExhibitList;
    private MediaServiceManager mediaServiceManager;
    private List<ExhibitBean> tempList;
    private MapReceiver mapReceiver;


    public static MapFragment newInstance(){
        if(mapFragment==null){
            return new MapFragment();
        }else{
            return mapFragment;
        }
    }
    public List<ExhibitBean> getTopicExhibitList() {
        return topicExhibitList;
    }

    public void setTopicExhibitList(List<ExhibitBean> topicExhibitList) {
        this.topicExhibitList = topicExhibitList;
    }

    @Override
    public void onAttach(Activity activity) {
        this.activity = activity;
        registerReceiver();
        /*bluetoothManager =new  BluetoothManager(activity);
        bluetoothManager.initBeaconSearcher();
        bluetoothManager.setNearestBeaconListener(nearestBeaconListener);*/
        mediaServiceManager=MediaServiceManager.getInstance(activity);
        handler = new MyHandler();
        super.onAttach(activity);
    }

    private void registerReceiver() {
        IntentFilter filter=new IntentFilter();
        filter.addAction(INTENT_BEACON);
        mapReceiver =new MapReceiver();
        activity.registerReceiver(mapReceiver,filter);
    }


    @Override
    void initView() {
        setContentView(R.layout.fragment_map);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        nextObjectId = 0;
        model = new MapObjectContainer();
        //initTestLocationPoints();
        initMap(savedInstanceState);
        //initModel();
        //initMapObjects();
        mapObjectInfoPopup = new TextPopup(activity, (FrameLayout) view);
        initMapListeners();
        //map.getLayerById(LAYER2_ID).setVisible(false);
        // 将在地图上显示用户的位置
        // 需要在manifest启用 ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION 权限
        //如果你在 Filitheyo island ,取消这个注释
        // map.setShowMyPosition(true);
        map.centerMap();
        if(topicExhibitList!=null){
            drawerTopicExhibitsPoint(topicExhibitList);
        }


    }

    //初始化地图
    private void initMap(Bundle savedInstanceState) {
        //为了在屏幕上显示需要的地图，需要初始化工具，并将其放入地图
        map = new MapWidget(savedInstanceState, activity,
                "map", // root name of the map under assets folder.
                11); // 地图初始缩放级别
        map.setId(MAP_ID);
        OfflineMapConfig config = map.getConfig();
        config.setMapCenteringEnabled(true);
        config.setPinchZoomEnabled(true); // 双指缩放启用
        config.setFlingEnabled(true);    // Sets inertial scrolling of the map
        config.setMinZoomLevelLimit(11); //最小缩放级别
        config.setMaxZoomLevelLimit(15); //最大缩放级别
        config.setZoomBtnsVisible(false); // 设置变焦按钮可见
        // Configuration of GPS receiver
        GPSConfig gpsConfig = config.getGpsConfig();
        gpsConfig.setPassiveMode(false);
        gpsConfig.setGPSUpdateInterval(500, 1);//最小时间(毫秒),最小距离(米)
        // Configuration of position marker
        //MapGraphicsConfig graphicsConfig = config.getGraphicsConfig();
        //graphicsConfig.setAccuracyAreaColor(0x550000FF); // Blue with transparency
        // graphicsConfig.setAccuracyAreaBorderColor(Color.BLUE); // Blue without transparency
        RelativeLayout layout = (RelativeLayout) contentView.findViewById(R.id.mapLayout);
        // Adding the map to the layout
        layout.addView(map, 0);
        layout.setBackgroundColor(Color.parseColor("#ffffff"));
        //要在地图上绘制点，需要先添加图层(通过图层的ID可以访问特定的图层)
        map.createLayer(PERSON_LAYER); //创建人员定位图层
        map.createLayer(EXHIBITS_LAYER);//创建展品显示图层
    }

    public void drawerTopicExhibitsPoint(List<ExhibitBean> exhibitList) {
        for(ExhibitBean bean:exhibitList){
            MapObjectModel objectModel = new MapObjectModel(bean.getId(),(int)bean.getMapx(), (int)bean.getMapy(),bean.getAddress());
            model.addObject(objectModel);
            /*for (Location point : points) {
                objectModel = new MapObjectModel(id, point, "Point " + id);
                model.addObject(objectModel);
                id += 1;
            }*/
        }
        initMapObjects(map.getLayerById(EXHIBITS_LAYER));
    }


    //初始化地图模型
    private void initModel() {
        if(beacon==null){return;}
        tempList=DataBiz.getExhibitListByBeaconId(beacon.getMuseumId(),beacon.getId());
        if(tempList==null||tempList.size()==0){return;}
        //创建并添加地图对象模型(图标点)
        MapObjectModel objectModel = new MapObjectModel(tempList.get(0).getId(), (int) beacon.getPersonx(), (int) beacon.getPersony(), beacon.getMinor());
        model.addObject(objectModel);

        //将对象添加到模型
        //你可能想实现自己的模型

//        MapObjectModel objectModel = new MapObjectModel(0, 550, 362, "第二号坑 铜车马陈列室");
//        model.addObject(objectModel);
//        objectModel = new MapObjectModel(1, 550, 360, "第一号坑 秦兵马俑");
//        model.addObject(objectModel);

//		int id = 2;
//		for (Location point:points) {
//			objectModel = new MapObjectModel(id, point, "Point " + id);
//			model.addObject(objectModel);
//			id += 1;
//		}
    }


    //初始化地图对象
    private void initMapObjects(Layer layer) {

        //mapObjectInfoPopup = new TextPopup(activity,(FrameLayout)view);
        //获取图层，并在该图层绘点
        //Layer personLayer = map.getLayerById(PERSON_LAYER);
        for (int i = 0; i < model.size(); ++i) {
            addNotScalableMapObject(model.getObject(i), layer);
        }
        //增加两个地图对象到第二层  右侧的两个点
//		addScalableMapObject(800, 100, layer2);//图标和图一起放大
//		addNotScalableMapObject(800, 350,layer2);//地图放大缩小图标点自动适应
    }

    //绘制不可扩展的地图对象
    private void addNotScalableMapObject(MapObjectModel objectModel, Layer layer) {
        if (objectModel.getLocation() != null) {
            addNotScalableMapObject(objectModel.getLocation(), layer);
        } else {
            addNotScalableMapObject(objectModel.getId(),objectModel.getX(), objectModel.getY(), layer);
        }
    }


    //绘制不可扩展的地图对象(x,y)
    private void addNotScalableMapObject(String id,int x, int y, Layer layer) {
        try{
            // Getting the drawable of the map object
            //Drawable drawable = getResources().getDrawable(R.drawable.maps_blue_dot);
            Drawable drawable =activity.getResources().getDrawable(R.drawable.maps_blue_dot);
            //绘制人员位置时，先清除改图层上的所有地图对象
            if (map.getLayerById(PERSON_LAYER) == layer){
                layer.clearAll();
                drawable = activity.getResources().getDrawable(R.drawable.icon_map_object);
            }

            pinHeight = drawable.getIntrinsicHeight();
            // Creating the map object
            MapObject object1 = new MapObject(id, // Integer.valueOf(nextObjectId)id, will be passed to the listener when user clicks on it
                    drawable,
                    new Point(x, y), // coordinates in original map coordinate system.
                    // Pivot point of center of the drawable in the drawable's coordinate system.
                    PivotFactory.createPivotPoint(drawable, PivotFactory.PivotPosition.PIVOT_CENTER),
                    true, // This object will be passed to the listener
                    false); // is not scalable. It will have the same size on each zoom level
            //绘制地图对象(图标点)到图层
            layer.addMapObject(object1);
            nextObjectId += 1;
        }catch (Exception e){
            ExceptionUtil.handleException(e);}

    }

    //绘制不可扩展的地图对象(location)
    private void addNotScalableMapObject(Location location, Layer layer) {
        try {
            if (location == null)
                return;
            // Getting the drawable of the map object
            //获取地图对象的绘制
            Drawable drawable = getResources().getDrawable(R.drawable.icon_map_object);// TODO: 2016/1/1
            // Creating the map object
            MapObject object1 = new MapObject(Integer.valueOf(nextObjectId), // id, will be passed to the listener when user clicks on it
                    drawable,
                    new Point(0, 0), // coordinates in original map coordinate system.
                    // Pivot point of center of the drawable in the drawable's coordinate system.
                    PivotFactory.createPivotPoint(drawable, PivotFactory.PivotPosition.PIVOT_CENTER),
                    true, // This object will be passed to the listener
                    true); // is not scalable. It will have the same size on each zoom level
            layer.addMapObject(object1);

            // Will crash if you try to move before adding to the layer.
            object1.moveTo(location);
            nextObjectId += 1;
        }catch (Exception e){
            ExceptionUtil.handleException(e);
        }
    }


    //绘制可扩展的地图对象
    private void addScalableMapObject(int x, int y, Layer layer) {
        Drawable drawable = getResources().getDrawable(R.drawable.maps_blue_dot);
        MapObject object1 = new MapObject(Integer.valueOf(nextObjectId),
                drawable,
                x,
                y,
                true,
                true);

        layer.addMapObject(object1);
        nextObjectId += 1;
    }

    //地图监听事件
    private void initMapListeners() {
        // In order to receive MapObject touch events we need to set listener
        map.setOnMapTouchListener(this);
        // In order to receive pre and post zoom events we need to set MapEventsListener
        map.addMapEventsListener(this);
        // 地图滚动监听器
        map.setOnMapScrolledListener(new OnMapScrollListener() {
            public void onScrolledEvent(MapWidget v, MapScrolledEvent event) {
                handleOnMapScroll(v, event);
            }
        });
        //位置变化监听器
        map.setOnLocationChangedListener(new OnLocationChangedListener() {
            @Override
            public void onLocationChanged(MapWidget v, Location location) {
                // You can handle location change here.
                // For example you can scroll to new location by using v.scrollMapTo(location)
            }
        });
    }

    //地图上的菜单栏
    //@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(activity);
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    //地图上菜单栏的点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.zoomIn:
                map.zoomIn();
                return true;
            case R.id.zoomOut:
                map.zoomOut();
                return true;
            case R.id.hidePersonLayer: {
                Layer layer = map.getLayerById(PERSON_LAYER);
                if (layer != null) {
                    layer.setVisible(false);
                    map.invalidate(); // Need to repaint the layer. This is a bug and will be fixed in next version.
                }
                return true;
            }
            case R.id.showPersonLayer: {
                Layer layer = map.getLayerById(PERSON_LAYER);
                if (layer != null) {
                    layer.setVisible(true);
                    map.invalidate(); // Need to repaint the layer. This is a bug and will be fixed in next version.
                }
                return true;
            }
//			case R.id.scroll_next:
//				map.scrollMapTo(getNextLocationPoint());
//				break;
        }

        return super.onOptionsItemSelected(item);
    }

    //地图滚动处理
    private void handleOnMapScroll(MapWidget v, MapScrolledEvent event) {
        // When user scrolls the map we receive scroll events
        // This is useful when need to move some object together with the map
        int dx = event.getDX(); // Number of pixels that user has scrolled horizontally
        int dy = event.getDY(); // Number of pixels that user has scrolled vertically
        if (mapObjectInfoPopup.isVisible()) {
            mapObjectInfoPopup.moveBy(dx, dy);
        }
    }

    //地图放大
    @Override
    public void onPostZoomIn() {
        Log.i(TAG, "onPostZoomIn()" + "---" + map.getZoomLevel());
        if(map.getZoomLevel()==14) {
            //map.getLayerById(EXHIBITS_LAYER).setVisible(true);
            //展品列表
          /* // List<ExhibitBean> list =  application.totalExhibitBeanList;
            MapObjectModel objectModel;
            for(ExhibitBean e:list){
                objectModel = new MapObjectModel(0, (int)e.getMapx(), (int)e.getMapy(), e.getName());
                model.addObject(objectModel);
            }*/// TODO: 2016/1/7
            // initModel();
            initMapObjects(map.getLayerById(EXHIBITS_LAYER));
        }

    }

    //地图缩小
    @Override
    public void onPostZoomOut() {
        Log.i(TAG, "onPostZoomOut()");
    }

    @Override
    public void onPreZoomIn() {
        Log.i(TAG, "onPreZoomIn()");
        if (mapObjectInfoPopup != null) {
            mapObjectInfoPopup.hide();
        }
    }

    @Override
    public void onPreZoomOut() {
        Log.i(TAG, "onPreZoomOut()");
        if (mapObjectInfoPopup != null) {
            mapObjectInfoPopup.hide();
        }
    }

    //* On map touch listener implemetnation 地图上的触摸监听器实现 *//
    @Override
    public void onTouch(MapWidget v, MapTouchedEvent event) {
        // Get touched object events from the MapTouchEvent
        ArrayList<ObjectTouchEvent> touchedObjs = event.getTouchedObjectIds();

        if (touchedObjs.size() > 0) {

            int xInMapCoords = event.getMapX();
            int yInMapCoords = event.getMapY();
            int xInScreenCoords = event.getScreenX();
            int yInScreenCoords = event.getScreenY();

            ObjectTouchEvent objectTouchEvent = event.getTouchedObjectIds().get(0);

            // Due to a bug this is not actually the layer id, but index of the layer in layers array.
            // Will be fixed in the next release.
            long layerId = objectTouchEvent.getLayerId();
            String objectId = String.valueOf(objectTouchEvent.getObjectId()) ;
            // User has touched one or more map object
            // We will take the first one to show in the toast message.
            String message = "You touched the object with id: " + objectId + " on layer: " + layerId +
                    " mapX: " + xInMapCoords + " mapY: " + yInMapCoords + " screenX: " + xInScreenCoords + " screenY: " +
                    yInScreenCoords;

            Log.d(TAG, message);

            MapObjectModel objectModel = model.getObjectById(objectId);

            if (objectModel != null) {
                // This is a case when we want to show popup info exactly above the pin image

                float density = getResources().getDisplayMetrics().density;
                int imgHeight = (int) (pinHeight / density / 2);

                // Calculating position of popup on the screen
                int x = xToScreenCoords(objectModel.getX());
                int y = yToScreenCoords(objectModel.getY()) - imgHeight;

                // Show it
                showLocationsPopup(x, y, objectModel);
            } else {
                // This is a case when we want to show popup where the user has touched.
                showLocationsPopup(xInScreenCoords, yInScreenCoords, objectModel);
            }

            // Hint: If user touched more than one object you can show the dialog in which ask
            // the user to select concrete object
        } else {
            if (mapObjectInfoPopup != null) {
                mapObjectInfoPopup.hide();
            }
        }
    }


    private void showLocationsPopup(int x, int y, MapObjectModel objectModel) {
        RelativeLayout mapLayout = (RelativeLayout) contentView.findViewById(R.id.mapLayout);

        if (mapObjectInfoPopup != null) {
            mapObjectInfoPopup.hide();
        }
        if(objectModel==null){
            Toast.makeText(activity,"objectModel为空",Toast.LENGTH_SHORT).show();
            return;}
        String exhibitId=objectModel.getId();
        final ExhibitBean exhibit=DataBiz.getExhibitFromDBById(exhibitId);
        if(exhibit==null){return;}
        ((TextPopup) mapObjectInfoPopup).setIcon((BitmapDrawable) getResources().getDrawable(R.drawable.icon_map_popup_arrow));
        ((TextPopup) mapObjectInfoPopup).setText(exhibit.getName());

        mapObjectInfoPopup.setOnClickListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (mapObjectInfoPopup != null) {
                        mapObjectInfoPopup.hide();
                        }
                    ExhibitBean bean = mediaServiceManager.getCurrentExhibit();
                    if (bean == null || !bean.equals(exhibit)) {
                        String str = JSON.toJSONString(exhibit);
                        Intent intent = new Intent();
                        intent.setAction(INTENT_EXHIBIT);
                        intent.putExtra(INTENT_EXHIBIT, str);
                        activity.sendBroadcast(intent);}

                }

                return false;
            }
        });

        ((TextPopup) mapObjectInfoPopup).show(mapLayout, x, y);
    }

    /***
     * Transforms coordinate in map coordinate system to screen coordinate system 地图坐标转换到屏幕坐标
     *
     * @param mapCoord - X in map coordinate in pixels.  参数mapCoord - X 地图像素坐标
     * @return X coordinate in screen coordinates. You can use this value to display any object on the screen. X是 返回的屏幕坐标，可以使用这个方法在平面上显示任何对象
     */
    private int xToScreenCoords(int mapCoord) {
        return (int) (mapCoord * map.getScale() - map.getScrollX());
    }

    private int yToScreenCoords(int mapCoord) {
        return (int) (mapCoord * map.getScale() - map.getScrollY());
    }

    /**
     * 蓝牙扫描对象
     */
    private MyHandler handler;


    /*//蓝牙模块返回最近的信标
    private NearestBeaconListener nearestBeaconListener = new NearestBeaconListener() {
        @Override
        public void nearestBeaconCallBack(BeaconBean b) {
            beacon = b;
            tempList= DataBiz.getExhibitListByBeaconId(beacon.getMuseumId(), beacon.getId());
            handler.sendEmptyMessage(MSG_WHAT_DRAW_POINT);
        }
    };*/


    private final int MSG_WHAT_DRAW_POINT = 1;

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_WHAT_DRAW_POINT) {
                //地图界面加载后绘点
                if (contentView != null) {
                    initModel();//创建地图对象(图标点)
                    initMapObjects(map.getLayerById(PERSON_LAYER));//绘制地图对象
                }
            }
        }
    }


    @Override
    public void onDestroy() {
        activity.unregisterReceiver(mapReceiver);
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        map.saveState(outState);
    }

    class MapReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action.equals(INTENT_BEACON)){
                String beaconJson=intent.getStringExtra(INTENT_BEACON);
                beacon =JSON.parseObject(beaconJson,BeaconBean.class);
                handler.sendEmptyMessage(MSG_WHAT_DRAW_POINT);
            }
        }
    }



//	private void initTestLocationPoints()
//	{
//		//points = new Location[0];
//		points = new Location[5];
//		for (int i=0; i<points.length; ++i) {
//			points[i] = new Location("test");
//		}
//		points[0].setLatitude(3.2127012756213316);
//		points[0].setLongitude(73.03406774997711);
//
//		points[1].setLatitude(3.2122245926560167);
//		points[1].setLongitude(73.03744733333588);
//
//		points[2].setLatitude(3.2112819380469135);
//		points[2].setLongitude(73.03983449935913);
//
//		points[3].setLatitude(3.2130494147249915);
//		points[3].setLongitude(73.03946435451508);
//
//		points[4].setLatitude(3.2148276002942713);
//		points[4].setLongitude(73.03796768188477);
//
//		currentPoint = 0;
//	}


//	private Location getNextLocationPoint()
//	{
//		if (currentPoint < points.length-1) {
//			currentPoint += 1;
//		} else {
//			currentPoint = 0;
//		}
//
//		return points[currentPoint];
//	}

}
