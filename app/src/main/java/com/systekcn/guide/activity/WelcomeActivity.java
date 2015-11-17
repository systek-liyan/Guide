package com.systekcn.guide.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.magic.mapdemo.R;


public class WelcomeActivity extends BaseActivity {

    private AlphaAnimation start_anima;
    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = View.inflate(this, R.layout.activity_welcome, null);
        setContentView(view);
        initialize();
    }

    private void initialize() {

        start_anima = new AlphaAnimation(0.3f, 1.0f);
        start_anima.setDuration(2000);
        view.startAnimation(start_anima);
        start_anima.setAnimationListener(listener);
    }

    private void redirectTo() {
        Intent intent = new Intent(WelcomeActivity.this,CityActivity.class);
        startActivity(intent);
        finish();
    }
    private Animation.AnimationListener listener=new Animation.AnimationListener() {

        @Override
        public void onAnimationStart(Animation animation) {

        }
        @Override
        public void onAnimationRepeat(Animation animation) {
        }
        @Override
        public void onAnimationEnd(Animation animation) {
            redirectTo();
        }
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_welcome, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
