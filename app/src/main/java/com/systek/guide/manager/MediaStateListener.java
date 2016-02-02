package com.systek.guide.manager;

import com.systek.guide.entity.ExhibitBean;

import java.util.List;

/**
 * Created by Qiang on 2016/1/9.
 *
 * 音乐状态监听器
 */
public interface MediaStateListener {

    void onMediaPause();
    void onMediaStart();
    void onMediaStop();
    void onSeekTo(int progress);
    void onExhibiChanged(ExhibitBean bean);
    void omRefreshExhibitBeanList(List<ExhibitBean> list);

}
