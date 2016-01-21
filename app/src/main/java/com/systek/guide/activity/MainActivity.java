package com.systek.guide.activity;


import android.app.WallpaperManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ViewFlipper;

import com.systek.guide.R;
import com.systek.guide.custom.swipeback.SwipeBackActivity;
import com.systek.guide.custom.swipeback.SwipeBackLayout;

public class MainActivity extends SwipeBackActivity {

    private ViewFlipper myViewFlipper;
    LayoutInflater inflater ;



   /* @Override
    protected void initialize(Bundle savedInstanceState) {
        super.initialize(savedInstanceState);

    }*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lock_screen);
        setDragEdge(SwipeBackLayout.DragEdge.LEFT);
        Drawable wallPaper = WallpaperManager.getInstance(this).getDrawable();
        this.getWindow().setBackgroundDrawable(wallPaper);

    }


    /* @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflater= LayoutInflater.from(this);
        View view =inflater.inflate(R.layout.activity_main, null);
        setContentView(view);
        myViewFlipper = (ViewFlipper) view.findViewById(R.id.myViewFlipper);


        myViewFlipper.setLongClickable(true);
        myViewFlipper.setOnTouchListener(this);

        View lockScreen=inflater.inflate(R.layout.lock_screen,null);
        View empty=inflater.inflate(R.layout.empty_layout,null);
        myViewFlipper.addView(lockScreen);
        myViewFlipper.addView(empty);

    }*/

}
