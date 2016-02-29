package com.systek.guide.custom;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.systek.guide.R;

/**
 * Created by Qiang on 2016/2/26.
 */
public class LoadingDialog {


    /**
     * 自定义的progressDialog
     * @param context 上下文
     * @param msg 加载数据时显示的信息
     * @return Dialog
     */
    @SuppressWarnings("deprecation")
    public static Dialog createLoadingDialog(Context context, String msg) {

        LayoutInflater inflater = LayoutInflater.from(context);
        //加载loading_dialog.xml
        View v = inflater.inflate(R.layout.progress_dialog, null);// 得到加载view

        // loading_dialog.xml中的LinearLayout
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);// 加载布局

        // loading_dialog.xml中的TextView
        TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);// 提示文字
        tipTextView.setText(msg);// 设置加载信息（如：加载中，请稍候...）

        // 创建自定义样式loading_dialog
        Dialog loadingDialog = new Dialog(context, R.style.WindowDialog);
        loadingDialog.setCancelable(false);// 不可以用“返回键”取消
        // 设置布局
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.FILL_PARENT));
        return loadingDialog;
    }

}
