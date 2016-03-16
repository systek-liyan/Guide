package com.systek.guide.activity;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.SeekBar;

import com.systek.guide.R;
import com.systek.guide.adapter.base.ViewPagerAdapter;
import com.systek.guide.entity.ExhibitBean;
import com.systek.guide.fragment.BaseFragment;
import com.systek.guide.fragment.IconImageFragment;
import com.systek.guide.fragment.LockScreenFragment;
import com.systek.guide.manager.MediaServiceManager;
import com.systek.guide.utils.LogUtil;

import java.util.ArrayList;

public class LockScreenActivity extends BaseActivity
        implements LockScreenFragment.OnFragmentInteractionListener,IconImageFragment.OnFragmentInteractionListener {



    private ViewPager lockScreenViewPager;

    /* // 定义一个GestureDetector(手势识别类)对象的引用
     private GestureDetector myGestureDetector;
     private float mPosX;
     private float mPosY;
     private float mCurPosX;
     private float mCurPosY;
     View.OnTouchListener onTouchListener=new View.OnTouchListener() {
         @Override
         public boolean onTouch(View v, MotionEvent event) {

             switch (event.getAction()) {

                 case MotionEvent.ACTION_DOWN:
                     mPosX = event.getX();
                     mPosY = event.getY();
                     break;
                 case MotionEvent.ACTION_MOVE:
                     mCurPosX = event.getX();
                     mCurPosY = event.getY();

                     break;
             }
             return true;
         }
     };
 */
    SeekBar.OnSeekBarChangeListener onSeekBarChangeListener=new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(!fromUser){return;}
            Intent intent=new Intent();
            intent.setAction(INTENT_SEEK_BAR_CHANG);
            intent.putExtra(INTENT_SEEK_BAR_CHANG,progress);
            sendBroadcast(intent);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
    private LockScreenFragment lockScreenFragment;
    private IconImageFragment iconImageFragment;
    private ExhibitBean currentExhibit;
    private MediaServiceManager mediaServiceManager;

    @Override
    protected void setView() {
        // View view =getLayoutInflater().inflate(R.layout.activity_lock_screen,null);
        setContentView(R.layout.activity_lock_screen);
        //myGestureDetector=new GestureDetector(this);
        //view.setOnTouchListener(onTouchListener);
        //setDragEdge(SwipeBackLayout.DragEdge.LEFT);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        lockScreenViewPager=(ViewPager)findViewById(R.id.lockScreenViewPager);
        Intent intent=getIntent();
        String exhibitStr=intent.getStringExtra(INTENT_EXHIBIT);
        /*if(!TextUtils.isEmpty(exhibitStr)){
            ExhibitBean bean= JSON.parseObject(exhibitStr, ExhibitBean.class);
            if(currentExhibit==null){
                currentExhibit=bean;
                lockScreenFragment.initData(currentExhibit);
            }else{
                if(currentExhibit.equals(bean)){
                    refreshView();
                }else {
                    lockScreenFragment.initData(currentExhibit);
                }
            }

        }else{
            currentExhibit=mediaServiceManager.getCurrentExhibit();
            refreshView();
        }*/

        if(!TextUtils.isEmpty(exhibitStr)){
            lockScreenFragment=LockScreenFragment.newInstance(exhibitStr);
        }else{
            lockScreenFragment=LockScreenFragment.newInstance(null);
        }
        iconImageFragment=IconImageFragment.newInstance(null,null);
        ArrayList<BaseFragment> fragments=new ArrayList<>();
        fragments.add(iconImageFragment);
        fragments.add(lockScreenFragment);
        ViewPagerAdapter viewPagerAdapter=new ViewPagerAdapter(getSupportFragmentManager(),fragments);
        lockScreenViewPager.setAdapter(viewPagerAdapter);
        //去除滑动到末尾时的阴影
        lockScreenViewPager.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);

        lockScreenViewPager.setCurrentItem(1);

        lockScreenViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position==0){
                    getActivity().finish();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

       /* handler=new MyHandler();
        mediaServiceManager=MediaServiceManager.getInstance(this);
        addListener();
       */

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /*private void initData() {
        if(currentExhibit==null){return;}
        handler.sendEmptyMessage(MSG_WHAT_CHANGE_EXHIBIT);
    }*/


    @Override
    protected void initView() {

    }

    void addListener() {
        //seekBarProgress.setOnSeekBarChangeListener(onSeekBarChangeListener);
    }

    @Override
    void initData() {

    }

    @Override
    void registerReceiver() {

    }

    @Override
    void unRegisterReceiver() {

    }

    @Override
    void refreshView() {
        LogUtil.i("ZHANG", "执行了refreshView");
    }

    @Override
    void refreshExhibit() {

    }

    @Override
    void refreshTitle() {

    }

    @Override
    void refreshViewBottomTab() {

    }

    @Override
    void refreshProgress() {

    }

    @Override
    void refreshIcon() {

    }

    @Override
    void refreshState() {

    }


    @Override
    protected void onResume() {
        super.onResume();
        /*if(mediaServiceManager.isPlaying()){
            handler.sendEmptyMessage(MSG_WHAT_CHANGE_PLAY_START);
        }else{
            handler.sendEmptyMessage(MSG_WHAT_CHANGE_PLAY_STOP);
        }*/



    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            return true;
        }else if(keyCode==KeyEvent.KEYCODE_HOME){
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        //handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }


    @Override
    public void onFragmentInteraction(ExhibitBean exhibit) {
        // TODO: 2016/2/25
    }
}
