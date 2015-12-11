package com.systekcn.guide.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Qiang on 2015/11/26.
 */
public class Dot extends View {
    private Paint paint;
    private int selectedColor = 0;
    private int unSelectColor = 0;
    private float scale = 0.0f;

    public Dot(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public Dot(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Dot(Context context) {
        this(context, null);
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    /**
     * 设置动画缩放率
     *
     * @param scale
     */
    public void setScale(float scale) {
        this.scale = scale;
        invalidate();
    }

    /**
     * 设置是否被选中
     *
     * @param selected
     */
    public void setSelected(boolean selected) {
        setScale(selected ? 1.0f : 0.0f);
    }

    /**
     * 必须是getResources().getColor(resId)
     *
     * @param color
     */
    @SuppressLint("ResourceAsColor")
    public void setColor(int unSelectColorResId, int selectColorResId) {
        selectedColor = getResources().getColor(selectColorResId);
        unSelectColor = getResources().getColor(unSelectColorResId);
        invalidate();
    }

    @Override
    @SuppressLint("DrawAllocation")
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        if (measuredHeight != 0 && measuredWidth != 0 && measuredHeight < measuredWidth) {
            int scope = (int) ((measuredWidth - measuredHeight) * scale);
            int scopeEdge = scope == 0 ? scope : scope / 2;
            int cx = measuredWidth / 2;
            int offset = measuredHeight / 2 + scopeEdge;
            RectF rectF = new RectF(cx - offset, 0, cx + offset, measuredHeight);
            canvas.drawColor(Color.TRANSPARENT);
            paint.setColor(scopeEdge == 0 ? unSelectColor : selectedColor);
            canvas.drawRoundRect(rectF, measuredHeight, measuredHeight, paint);
        }
    }
}
