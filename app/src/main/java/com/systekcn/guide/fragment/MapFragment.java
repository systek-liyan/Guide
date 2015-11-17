package com.systekcn.guide.fragment;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.magic.map.IMapLoaderCallback;
import com.magic.map.MapManager;
import com.magic.map.resource.poi.PoiFilter;
import com.magic.map.util.MapData;
import com.magic.map.util.PoiData;
import com.magic.map.widget.MapView;
import com.magic.map.widget.onMapListener;
import com.magic.mapdemo.R;
import com.systekcn.guide.MyApplication;
import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.common.utils.LogUtil;

public class MapFragment extends Fragment implements IMapLoaderCallback,
        onMapListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener,IConstants{

    private static final String TAG = "MapDemo";
    private MapManager mManager = null;
    private MapView mMapView = null;
    private PoiFilter mFilter = null;
    private TextView mPoiName = null;
    private TextView mPoiDescription = null;
    private TextView mPoiPhone = null;
    private TextView mPoiUri = null;
    private TextView mPoiLocation = null;
    private ViewGroup mPoi = null;
    private EditText mCustomPoiId = null;
    private EditText mCustomPoiName = null;
    private EditText mCustomPoiDescription = null;
    private EditText mCustomPoiPhone = null;
    private EditText mCustomPoiSite = null;
    private Button mBtnStart = null;
    private Button mBtnEnd = null;
    private Button mBtnDel = null;
    private Button mBtnRoute = null;
    private ToggleButton mBtnPoiSwitcher = null;
    private PointF mTouchPoint = null;
    private long mPoiId = 0;
    private PoiData mStart = null;
    private PoiData mEnd = null;
    private PoiData mPoiData = null;

    private Activity activity;
    private View view;

    private MyApplication application;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity=activity;
        application= (MyApplication) activity.getApplication();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_map, container, false);
        /*try{
            mManager = new MapManager(activity);
            mManager.init();
            mManager.addOnMapListener(this);
            initComponents();
        }catch (Exception e){
            ExceptionUtil.handleException(e);
        }*/
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mMapView!=null){
            mMapView.onResume();
        }
    }

    @Override
    public void onPause() {
        if(mMapView!=null){
            mMapView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onMapInit(MapData data) {
        LogUtil.i(TAG, getString(R.string.map_init));
    }

    @Override
    public void onMapIndexFinish() {
        LogUtil.i(TAG, getString(R.string.map_index_finish));
    }

    @Override
    public void onMapLoadFinish(int i) {
        LogUtil.i(TAG, "onMapLoadFinish ");
    }

    @Override
    public void onMapFinish() {
        LogUtil.i(TAG, getString(R.string.map_finish));
    }

    @Override
    public void onAmenityLoadFinish(int error) {
        LogUtil.i(TAG, getString(R.string.amenity_load_finish));
    }

    @Override
    public void onPoiClick(PoiData data) {
        mPoiName.setText(data.getName());
        mPoiDescription.setText(data.getDescription());
        //此处修改phone为显示id
        mPoiPhone.setText("Phone Num:" + data.getId());
        mPoiUri.setText("Site:" + data.getUri());
        mPoiLocation.setText(String.format("Lat:%f, Lon:%f", data.getLat(),
                data.getLon()));
        mPoiId = data.getId();
        	if (data.getType() == PoiFilter.TYPE_CUSTOM) {
        		mBtnDel.setEnabled(true);
        	} else {
        		mBtnDel.setEnabled(false);
        	}
        if (!mPoi.isShown()) {
            mPoi.setVisibility(View.VISIBLE);
        }
    }

    private void initComponents() {
        mMapView = (MapView) findViewById(R.id.mapview);
        mPoiName = (TextView) findViewById(R.id.tv_name);
        mPoiDescription = (TextView) findViewById(R.id.tv_description);
        mPoiPhone = (TextView) findViewById(R.id.tv_phone);
        mPoiUri = (TextView) findViewById(R.id.tv_uri);
        mPoiLocation = (TextView) findViewById(R.id.tv_location);
        mPoi = (ViewGroup) findViewById(R.id.poi_info);
        mMapView.setMapManager(mManager);
        mFilter = new PoiFilter(mManager, PoiFilter.TYPE_SHOP);
        mBtnStart = (Button) findViewById(R.id.btn_start);
        mBtnEnd = (Button) findViewById(R.id.btn_stop);
        mBtnDel = (Button) findViewById(R.id.btn_del);
        mBtnRoute = (Button) findViewById(R.id.btn_route);
        mBtnPoiSwitcher = (ToggleButton) findViewById(R.id.tbn_poi);
        mBtnStart.setOnClickListener(this);
        mBtnEnd.setOnClickListener(this);
        mBtnDel.setOnClickListener(this);
        mBtnRoute.setOnClickListener(this);
        mBtnPoiSwitcher.setOnCheckedChangeListener(this);
        mMapView.setOnPoiListener(this);
    }

    private View findViewById(int id){
        return view.findViewById(id);
    }

    public void onNothingClick() {
        if (mPoi.isShown()) {
            mPoi.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLongPress(PointF point) {
        mTouchPoint = point;
        if (mBtnPoiSwitcher.isChecked()) {
            onCreateDialog(0).show();
        }
    }

    protected Dialog onCreateDialog(int id) {
        Builder builder = new Builder(activity);
        LayoutInflater inflater = LayoutInflater.from(activity);
        View layout = null;
        if (id == 0) {
            layout = inflater.inflate(R.layout.custom_poi, null);
            mCustomPoiId = (EditText) layout.findViewById(R.id.et_id);
            mCustomPoiName = (EditText) layout.findViewById(R.id.et_name);
            mCustomPoiDescription = (EditText) layout
                    .findViewById(R.id.et_description);
            mCustomPoiPhone = (EditText) layout.findViewById(R.id.et_phone);
            mCustomPoiSite = (EditText) layout.findViewById(R.id.et_site);
            builder.setView(layout);
            builder.setPositiveButton(getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String id = mCustomPoiId.getText().toString();
                            if (id.length() > 0) {
                                PoiData data = new PoiData(Long.parseLong(id),
                                        mCustomPoiName.getText().toString(),
                                        mCustomPoiDescription.getText()
                                                .toString(), mCustomPoiPhone
                                        .getText().toString(),
                                        mCustomPoiSite.getText().toString(),
                                        PoiFilter.TYPE_CUSTOM);
                                mMapView.addPoi(mTouchPoint, data);
                                dialog.dismiss();
                            } else {
                                mCustomPoiId
                                        .setError(getString(R.string.error_id));
                            }
                        }
                    });
            builder.setNegativeButton(getString(android.R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
        } else if (id == 1) {
            layout = inflater.inflate(R.layout.error, null);
            builder.setView(layout);
            builder.setPositiveButton(getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
        }
        return builder.create();
    }

    @Override
    public void onMapLoadError(int arg0) {
        onCreateDialog(1).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_del:
                mMapView.delPoi(mPoiId);
                mPoi.setVisibility(View.INVISIBLE);
                break;
            case R.id.btn_start:
                mStart = mPoiData;
                mBtnRoute.setEnabled(checkRouteReadly());
                break;
            case R.id.btn_stop:
                mEnd = mPoiData;
                mBtnRoute.setEnabled(checkRouteReadly());
                break;
            case R.id.btn_route:
                mMapView.getController().searchRoute(mStart, mEnd);
                break;
        }
    }

    private boolean checkRouteReadly(){
        return mStart != null && mEnd != null;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            mMapView.setFilter(mFilter);
        } else {
            mMapView.setFilter(null);
        }
    }

    @Override
    public void onNewRouteCalculated(boolean arg0) {
        // TODO Auto-generated method stub

    }

}
