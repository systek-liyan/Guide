package com.systekcn.guide.lyric;

import java.util.List;

/**
 * Created by Qiang on 2015/10/29.
 */
public class LyricModel {

    private List<SentenceModel> sentenceList;

    public LyricModel(List<SentenceModel> list)
    {
        this.sentenceList = list;
    }

    public List<SentenceModel> getSentenceList()
    {
        return sentenceList;
    }

    /**
     * 得到当前正在播放的那一句的下标 不可能找不到，因为最开头要加一句 自己的句子 ，所以加了以后就不可能找不到了
     *
     * @return 下标
     */
    public int getNowSentenceIndex(long t) {
        for (int i = 0; i < sentenceList.size(); i++) {
            if (sentenceList.get(i).isInTime(t)) {
                return i;
            }
        }
        // throw new RuntimeException("竟然出现了找不到的情况！");
        return -1;
    }

}
