package com.systekcn.guide.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.magic.mapdemo.R;
import com.systekcn.guide.widget.slidingmenu.SlidingMenu;

public class CollectionActivity extends BaseActivity {

    private ImageView iv_collection_drawer;
    private SlidingMenu side_drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);
        initialize();
    }

    private void initialize() {
        initView();
        addListener();
    }

    private void addListener() {
        iv_collection_drawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_collection_drawer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (side_drawer.isMenuShowing()) {
                            side_drawer.showContent();
                        } else {
                            side_drawer.showMenu();
                        }
                    }
                });

            }
        });
    }

    private void initView() {
        iv_collection_drawer=(ImageView)findViewById(R.id.iv_collection_drawer);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(side_drawer.isMenuShowing() ||side_drawer.isSecondaryMenuShowing()){
                side_drawer.showContent();
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
