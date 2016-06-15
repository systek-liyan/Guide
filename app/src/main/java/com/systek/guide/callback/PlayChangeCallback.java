package com.systek.guide.callback;

import com.systek.guide.entity.ExhibitBean;

/**
 * Created by Qiang on 2016/5/26.
 */
public interface PlayChangeCallback {

    int STATE_INVALID =0;
    int STATE_PLAYING =1;
    int STATE_PAUSE=2;
    int STATE_STOP=3;

    void onStateChanged(int state);
    void onExhibitChanged(ExhibitBean exhibit);
    void onPositionChanged(int duration ,int position);

}
