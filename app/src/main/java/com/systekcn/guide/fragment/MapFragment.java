package com.systekcn.guide.fragment;

import android.support.v4.app.Fragment;

import com.systekcn.guide.common.IConstants;

public class MapFragment extends Fragment implements IConstants{

    /*private static final String TAG = "MapDemo";
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
    public float nnnn=0;
    public float mmmm=0;
    private MapController mController;
    private double cc;
    private double dd;
    private double ee;
    private double ff;
    private float aaa;
    private int bbb;
    private float ccc;
    private int count;//自定义count

    private MyApplication application;
    private Double exhibitX;
    private Double exhibitY;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity=activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application= (MyApplication) activity.getApplication();
        ExhibitBean bean=application.currentExhibitBean;
        exhibitX = Double.valueOf(application.currentExhibitBean.getMapx());
        exhibitY=Double.valueOf(application.currentExhibitBean.getMapy());
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_map, container, false);
        try{
            mManager = new MapManager(activity);
            mManager.init();
            mManager.addOnMapListener(this);
            initComponents();
        }catch (Exception e){
            ExceptionUtil.handleException(e);
        }
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
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onMapInit(MapData data) {
       // LogUtil.i("ZHANG", getString(R.string.map_init));

    }

    @Override
    public void onMapIndexFinish() {
        LogUtil.i("ZHANG", getString(R.string.map_index_finish));
    }

    @Override
    public void onMapLoadFinish(int i) {
        count++;
       // LogUtil.i("ZHANG", "onMapLoadFinish ");
        exhibitX = Double.valueOf(application.currentExhibitBean.getMapx());
        exhibitY=Double.valueOf(application.currentExhibitBean.getMapy());
        handler.sendEmptyMessage(MSG_WHAT_DRAW_EXHIBIT);
    }
    private  final int MSG_WHAT_DRAW_EXHIBIT=1;
    Handler handler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==MSG_WHAT_DRAW_EXHIBIT){
                renwubiaozhu(exhibitX,exhibitY);
            }
        }
    };

    @Override
    public void onMapFinish() {
        LogUtil.i("ZHANG", getString(R.string.map_finish));
        zhanpinbiaozhu(exhibitX, exhibitY, count);
    }

    @Override
    public void onAmenityLoadFinish(int error) {
        LogUtil.i("ZHANG", getString(R.string.amenity_load_finish));
    }

    @Override
    public void onPoiClick(PoiData data) {
        //  mPoiName.setText(data.getName());
        //  mPoiDescription.setText(data.getDescription());
        //此处修改phone为显示id
        //  mPoiPhone.setText("Phone Num:" + data.getId());
        //  mPoiUri.setText("Site:" + data.getUri());
        // mPoiLocation.setText(String.format("Lat:%f, Lon:%f", data.getLat(),
        //          data.getLon()));
        // mPoiId = data.getId();
        //	if (data.getType() == PoiFilter.TYPE_CUSTOM) {
        //		mBtnDel.setEnabled(true);
        //	} else {.0.
        //		mBtnDel.setEnabled(false);
        //	}
        //  if (!mPoi.isShown()) {
        //       mPoi.setVisibility(View.VISIBLE);
        //   }
    }

    private void initComponents() {
        try{
            mMapView = (MapView) findViewById(R.id.mapview);
            mController=mMapView.getController();
            mController.setZoomAnimate(0, 1);
            mMapView.setMapManager(mManager);
            mFilter = new PoiFilter(mManager, 2);
            mMapView.setOnPoiListener(this);
        }catch ( Exception e){
            ExceptionUtil.handleException(e);
        }
    }

    private View findViewById(int id){
        return view.findViewById(id);
    }

    public void onNothingClick() {
        if (mPoi!=null&&mPoi.isShown()) {
            mPoi.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLongPress(PointF point) {
        LogUtil.i("ZHANG","onLongPress");
    }

    @Override
    public void onMapLoadError(int arg0) {
        //onCreateDialog(1).show();// TODO: 2015/11/17
    }

    @Override
    public void onClick(View v) {
        LogUtil.i("ZHANG","onClick");
    }

    private boolean checkRouteReadly(){
        return mStart != null && mEnd != null;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        LogUtil.i("ZHANG","onCheckedChanged");
        if (isChecked) {
            mMapView.setFilter(mFilter);
        } else {
            mMapView.setFilter(null);
        }
    }

    @Override
    public void onNewRouteCalculated(boolean arg0) {
        LogUtil.i("ZHANG","onNewRouteCalculated");
    }

    //这是搜索后的展品位置标注，参数x，y是想在地图上标出的坐标，count用来计数同时生成id
    public void zhanpinbiaozhu(double x,double y,int count){
        Builder builder = new Builder(activity);
        LayoutInflater inflater = LayoutInflater.from(activity);
        View layout = null;
        layout = inflater.inflate(R.layout.custom_poi, null);
        //mCustomPoiId = (EditText) layout.findViewById(R.id.et_id);
        mCustomPoiName = (EditText) layout.findViewById(R.id.et_name);
        mCustomPoiDescription = (EditText) layout.findViewById(R.id.et_description);
        mCustomPoiPhone = (EditText) layout.findViewById(R.id.et_phone);
        mCustomPoiSite = (EditText) layout.findViewById(R.id.et_site);
        builder.setView(layout);
        PoiData data = new PoiData(111112345+count,
                mCustomPoiName.getText().toString(),
                mCustomPoiDescription.getText().toString(),
                mCustomPoiPhone.getText().toString(),
                mCustomPoiSite.getText().toString(),
                PoiFilter.TYPE_SHOP);

        //x=116.356738,y=39.959162;x,y是坐标值，格式大概是这样
        //(570,810)是mMapView在任何情况下获取坐标对应的屏幕位置！！！
        //这是在1920屏幕上测试通过的，假如是小屏幕不通过，只需修改（579,810）这一组数值即可。
        cc=mMapView.getLatitude();
        dd=mMapView.getLongitude();
        ee=x-dd;
        ff=cc-y;
        aaa=mMapView.getDensity();
        bbb=mMapView.getZoom();
        ccc=mMapView.getZoomScale();
        //算式后的系数，一为17(0,1)倍率下，x轴参数，一为y轴参数。
        //float n=(float)(570+ee*186431.9);
        //float m=(float)(810+ff*243191.9);
        float n=(float)(570+ee*186431.9);
        float m=(float)(810+ff*243191.9);
        mTouchPoint=new PointF(n,m);
        mMapView.addPoi(mTouchPoint, data);
    }

    public void renwubiaozhu(double x,double y){
        cc=mMapView.getLatitude();
        dd=mMapView.getLongitude();
        ee=x-dd;
        ff=cc-y;
        aaa=mMapView.getDensity();
        bbb=mMapView.getZoom();
        ccc=mMapView.getZoomScale();
        //算式后的系数，一为17(0,1)倍率下，x轴参数，一为y轴参数。
        //float n=(float)(570+ee*186431.9);
        //float m=(float)(810+ff*243191.9);
        nnnn=(float)(570+ee*186431.9);
        mmmm=(float)(810+ff*243191.9);
        FrameLayout root = (FrameLayout)view;
        final DrawView draw = new DrawView(activity);
        draw.setMinimumWidth(300);
        draw.setMinimumHeight(500);
        root.addView(draw);
    }*/

}
