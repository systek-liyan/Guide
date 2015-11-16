package com.systekcn.guide.adapter;

import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by Qiang on 2015/11/2.
 */
public interface DownloadProgressListener {
    void onProgressChanged(ProgressBar progressBar,TextView textView,ImageView imageview);
}
