package com.systekcn.guide.widget;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;


public class DrawView extends View
{
	public float currentX = 600;
	public float currentY = 900;
	Paint p = new Paint();
	/**
	 * @param context
	 */
	public DrawView(Context context)
	{
		super(context);
		// TODO Auto-generated constructor stub
	}
	@Override
	public void onDraw (Canvas canvas)
	{
		super.onDraw(canvas);

		p.setColor(Color.RED);
		canvas.drawCircle(currentX , currentY , 30 , p);
	}
}
