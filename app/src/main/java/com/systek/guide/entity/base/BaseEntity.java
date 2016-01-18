package com.systek.guide.entity.base;

import java.io.Serializable;

/**
 * Created by Qiang on 2015/11/26.
 */
public abstract class BaseEntity implements Serializable {
    /**
     * serialVersionUID
     */
    public static final long serialVersionUID = 1L;

    private long mId = 0;

    public long getmId() {
        if (0 == mId) {
            mId = System.currentTimeMillis();
        }
        return mId;
    }

    public void setmId(long mId) {
        this.mId = mId;
    }

    public abstract void parseData(String data);

    public abstract String getDataStr();
}
