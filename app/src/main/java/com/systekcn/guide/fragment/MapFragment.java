package com.systekcn.guide.fragment;

import android.app.Activity;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.magic.map.IMapLoaderCallback;
import com.magic.map.MapManager;
import com.magic.map.resource.poi.PoiFilter;
import com.magic.map.util.MapData;
import com.magic.map.view.MapController;
import com.magic.map.widget.MapView;
import com.systekcn.guide.R;
import com.systekcn.guide.common.IConstants;

public class MapFragment extends Fragment implements IMapLoaderCallback ,View.OnClickListener,IConstants{
    private static final String TAG = "Map1";
    private PoiFilter mFilter = null;
    private TextView mPoiName = null;
    private TextView mPoiDescription = null;
    private TextView mPoiPhone = null;
    private TextView mPoiUri = null;
    private TextView mPoiLocation = null;
    private ViewGroup mPoi = null;
    //	private EditText mCustomPoiId = null;
    private EditText mCustomPoiName = null;
    private EditText mCustomPoiDescription = null;
    private EditText mCustomPoiPhone = null;
    private EditText mCustomPoiSite = null;
    private Button mBtnStart = null;
    private Button mBtnEnd = null;
    private Button mBtnDel = null;

    private PointF mTouchPoint = null;
    private long mPoiId = 0;
    private MapController mController;
    private double cc;
    private double dd;
    private double ee;
    private double ff;
    private float aaa;
    private float ccc;
    private int bbb;

    private Activity activity;
    private MapManager mManager = null;
    private MapView mMapView = null;
    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        return fragment;
    }

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_map, container, false);
        mMapView = (MapView) view.findViewById(R.id.mapview);
        mManager = new MapManager(activity);
        mManager.init();
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity=activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public void onMapInit(MapData mapData) {

    }

    @Override
    public void onMapIndexFinish() {

    }

    @Override
    public void onMapLoadFinish(int i) {

    }

    @Override
    public void onMapFinish() {

    }

    @Override
    public void onAmenityLoadFinish(int i) {

    }

    @Override
    public void onMapLoadError(int i) {

    }

    @Override
    public void onClick(View v) {

    }
}
