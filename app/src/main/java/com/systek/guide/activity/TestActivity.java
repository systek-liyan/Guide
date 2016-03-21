package com.systek.guide.activity;

import android.app.Activity;
import android.os.Bundle;

import com.systek.guide.R;

public class TestActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
    }
}
