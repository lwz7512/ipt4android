package com.pintu.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.widget.ImageView;

public class StateImage extends ImageView {

	private int state = 0;
	private int border = 2;
	
	public StateImage(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		RectF rect = new RectF(-2, -2, this.getWidth(), this.getHeight());
        Paint mFramePaint = new Paint();
        mFramePaint.setAntiAlias(true);
        mFramePaint.setStyle(Paint.Style.STROKE);
        mFramePaint.setStrokeWidth(border);
        //TODO, 这里的颜色将来要换成数据，FF为不透明
        mFramePaint.setColor(0x00FF00FF);
        
        canvas.drawRect(rect, mFramePaint);
	}

}
