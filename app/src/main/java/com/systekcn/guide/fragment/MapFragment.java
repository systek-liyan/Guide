package com.systekcn.guide.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.systekcn.guide.MyApplication;
import com.systekcn.guide.R;
import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.common.map.MapObjectContainer;
import com.systekcn.guide.common.map.MapObjectModel;
import com.systekcn.guide.common.map.TextPopup;
import com.systekcn.guide.entity.ExhibitBean;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment implements IConstants,MapEventsListener,OnMapTouchListener {
    private View view;

    private static final String TAG = "BrowseMapActivity";

    private static final Integer LAYER1_ID = 0;
    private static final Integer LAYER2_ID = 1;
    private static final int MAP_ID = 23;

    private int nextObjectId;
    private int pinHeight;

    private MapObjectContainer model;
    private MapWidget map;
    private TextPopup mapObjectInfoPopup;
    private Activity activity;
    private Location points[];
    private int currentPoint;
    private MyApplication application;


    @Override
    public void onAttach(Activity activity) {
        this.activity=activity;
        application=MyApplication.get();
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.fragment_map, container, false);
        nextObjectId = 0;

//		model = new MapObjectContainer();

        //initTestLocationPoints();
        initMap(savedInstanceState);
//		initModel();
//		initMapObjects();
        mapObjectInfoPopup = new TextPopup(activity,(FrameLayout)view);

        initMapListeners();

        //map.getLayerById(LAYER2_ID).setVisible(false);

        // 将在地图上显示用户的位置
        // 需要在manifest启用 ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION 权限

        //如果你在 Filitheyo island ,取消这个注释
        map.setShowMyPosition(true);

        map.centerMap();

        return view;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        map.saveState(outState);
    }

//	private void initTestLocationPoints()
//	{
//		//points = new Location[0];
//		points = new Location[5];
//		for (int i=0; i<points.length; ++i) {
//			points[i] = new Location("test");
//		}
//
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


    //初始化地图
    private void initMap(Bundle savedInstanceState)
    {
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

        RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.mapLayout);
        // Adding the map to the layout
        layout.addView(map, 0);
        layout.setBackgroundColor(Color.parseColor("#ffffff"));

        // Adding layers in order to put there some map objects
        map.createLayer(LAYER1_ID); // you will need layer id's in order to access particular layer
        map.createLayer(LAYER2_ID);
    }



    //初始化地图模型
    private void initModel()
    {
        //需要在地图上显示的点
        List<ExhibitBean> list =  application.currentExhibitBeanList;
        MapObjectModel objectModel;
        for(ExhibitBean e:list){
            objectModel = new MapObjectModel(0, 550, 362, "第二号坑 铜车马陈列室");
            model.addObject(objectModel);
        }


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
    private void initMapObjects()
    {

//		mapObjectInfoPopup = new TextPopup(this, (RelativeLayout)findViewById(R.id.rootLayout));

        //Layer layer1 = map.getLayerById(LAYER1_ID);
        Layer layer2 = map.getLayerById(LAYER2_ID);

        for (int i=0; i<model.size(); ++i) {
            addNotScalableMapObject(model.getObject(i), layer2);
        }

        // Adding two map objects to the second layer
        //增加两个地图对象到第二层  右侧的两个点
//		addScalableMapObject(800, 100, layer2);//图标和图一起放大
//		addNotScalableMapObject(800, 350,layer2);//地图放大缩小图标点自动适应
    }


    //添加不可扩展的地图对象
    private void addNotScalableMapObject(int x, int y,  Layer layer)
    {
        // Getting the drawable of the map object
        Drawable drawable = getResources().getDrawable(R.drawable.map_object);
        pinHeight = drawable.getIntrinsicHeight();
        // Creating the map object
        MapObject object1 = new MapObject(Integer.valueOf(nextObjectId), // id, will be passed to the listener when user clicks on it
                drawable,
                new Point(x, y), // coordinates in original map coordinate system.
                // Pivot point of center of the drawable in the drawable's coordinate system.
                PivotFactory.createPivotPoint(drawable, PivotFactory.PivotPosition.PIVOT_CENTER),
                true, // This object will be passed to the listener
                false); // is not scalable. It will have the same size on each zoom level

        // Adding object to layer
        layer.addMapObject(object1);
        nextObjectId += 1;
    }

    //添加不可扩展的地图对象
    private void addNotScalableMapObject(MapObjectModel objectModel,  Layer layer)
    {
        if (objectModel.getLocation() != null) {
            addNotScalableMapObject(objectModel.getLocation(), layer);
        } else {
            addNotScalableMapObject(objectModel.getX(), objectModel.getY(),  layer);
        }
    }


    //添加不可扩展的地图对象
    private void addNotScalableMapObject(Location location, Layer layer) {
        if (location == null)
            return;

        // Getting the drawable of the map object
        //获取地图对象的绘制
        Drawable drawable = getResources().getDrawable(R.drawable.map_object);
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
    }


    //添加可扩展的地图对象
    private void addScalableMapObject(int x, int y, Layer layer)
    {
        Drawable drawable = getResources().getDrawable(R.drawable.map_object);
        MapObject object1 = new MapObject(Integer.valueOf(nextObjectId),
                drawable,
                x,
                y,
                true,
                true);

        layer.addMapObject(object1);
        nextObjectId += 1;
    }


    private void initMapListeners()
    {
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


    //@Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = new MenuInflater(activity);
        inflater.inflate(R.menu.menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.zoomIn:
                map.zoomIn();
                return true;
            case R.id.zoomOut:
                map.zoomOut();
                return true;
            case R.id.hideLayer2: {
                Layer layer = map.getLayerById(LAYER2_ID);
                if (layer != null) {
                    layer.setVisible(false);
                    map.invalidate(); // Need to repaint the layer. This is a bug and will be fixed in next version.
                }
                return true;
            }
            case R.id.showLayer2: {
                Layer layer = map.getLayerById(LAYER2_ID);
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
    private void handleOnMapScroll(MapWidget v, MapScrolledEvent event)
    {
        // When user scrolls the map we receive scroll events
        // This is useful when need to move some object together with the map

        int dx = event.getDX(); // Number of pixels that user has scrolled horizontally
        int dy = event.getDY(); // Number of pixels that user has scrolled vertically

        if (mapObjectInfoPopup.isVisible()) {
            mapObjectInfoPopup.moveBy(dx, dy);
        }
    }



    @Override
    public void onPostZoomIn() {
        Log.i(TAG, "onPostZoomIn()" + "---" + map.getZoomLevel());
//		if(map.getZoomLevel()==14) {
//			map.getLayerById(LAYER2_ID).setVisible(true);
//		}
        model = new MapObjectContainer();
        initModel();
        initMapObjects();
    }

    @Override
    public void onPostZoomOut() {
        Log.i(TAG, "onPostZoomOut()");
    }

    @Override
    public void onPreZoomIn()
    {
        Log.i(TAG, "onPreZoomIn()");

        if (mapObjectInfoPopup != null) {
            mapObjectInfoPopup.hide();
        }
    }

    @Override
    public void onPreZoomOut()
    {
        Log.i(TAG, "onPreZoomOut()");

        if (mapObjectInfoPopup != null) {
            mapObjectInfoPopup.hide();
        }
    }


    //* On map touch listener implemetnation 地图上的触摸监听器实现 *//
    @Override
    public void onTouch(MapWidget v, MapTouchedEvent event)
    {
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
            Integer objectId = (Integer)objectTouchEvent.getObjectId();
            // User has touched one or more map object
            // We will take the first one to show in the toast message.
            String message = "You touched the object with id: " + objectId + " on layer: " + layerId +
                    " mapX: " + xInMapCoords + " mapY: " + yInMapCoords + " screenX: " + xInScreenCoords + " screenY: " +
                    yInScreenCoords;

            Log.d(TAG, message);

            MapObjectModel objectModel = model.getObjectById(objectId.intValue());

            if (objectModel != null) {
                // This is a case when we want to show popup info exactly above the pin image

                float density = getResources().getDisplayMetrics().density;
                int imgHeight = (int) (pinHeight / density / 2);

                // Calculating position of popup on the screen
                int x = xToScreenCoords(objectModel.getX());
                int y = yToScreenCoords(objectModel.getY()) - imgHeight;

                // Show it
                showLocationsPopup(x, y, objectModel.getCaption());
            } else {
                // This is a case when we want to show popup where the user has touched.
                showLocationsPopup(xInScreenCoords, yInScreenCoords, "Shows where user touched");
            }

            // Hint: If user touched more than one object you can show the dialog in which ask
            // the user to select concrete object
        } else {
            if (mapObjectInfoPopup != null) {
                mapObjectInfoPopup.hide();
            }
        }
    }


    private void showLocationsPopup(int x, int y, String text)
    {
        RelativeLayout mapLayout = (RelativeLayout) view.findViewById(R.id.mapLayout);

        if (mapObjectInfoPopup != null)
        {
            mapObjectInfoPopup.hide();
        }

        ((TextPopup) mapObjectInfoPopup).setIcon((BitmapDrawable) getResources().getDrawable(R.drawable.map_popup_arrow));
        ((TextPopup) mapObjectInfoPopup).setText(text);

        mapObjectInfoPopup.setOnClickListener(new View.OnTouchListener()
        {
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    if (mapObjectInfoPopup != null)
                    {
                        mapObjectInfoPopup.hide();
                    }
                }

                return false;
            }
        });

        ((TextPopup) mapObjectInfoPopup).show(mapLayout, x, y);
    }

    /***
     * Transforms coordinate in map coordinate system to screen coordinate system 地图坐标转换到屏幕坐标
     * @param mapCoord - X in map coordinate in pixels.  参数mapCoord - X 地图像素坐标
     * @return X coordinate in screen coordinates. You can use this value to display any object on the screen. X是 返回的屏幕坐标，可以使用这个方法在平面上显示任何对象
     */
    private int xToScreenCoords(int mapCoord)
    {
        return (int)(mapCoord *  map.getScale() - map.getScrollX());
    }

    private int yToScreenCoords(int mapCoord)
    {
        return (int)(mapCoord *  map.getScale() - map.getScrollY());
    }



}
