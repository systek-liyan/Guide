package com.systekcn.guide.manager;

import com.systekcn.guide.entity.ExhibitBean;

import java.util.List;

/**
 * Created by Qiang on 2016/1/9.
 */
public interface MediaStateListener {

    void onMediaPause();
    void onMediaStart();
    void onMediaStop();
    void onSeekTo(int progress);
    void onExhibiChanged(ExhibitBean bean);
    void omRefreshExhibitBeanList(List<ExhibitBean> list);

}
