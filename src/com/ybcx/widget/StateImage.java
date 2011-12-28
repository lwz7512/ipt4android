package com.ybcx.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.util.Log;
import android.widget.ImageView;

public class StateImage extends ImageView {

	private static String TAG = "StateImage";
	
	//0: 默认状态
	//1: 有故事状态
	//2: 热图状态
	//3: 经典状态
	private int state = 0;
	
	public StateImage(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	
	//外面可以根据数据设置：
	//0，新鲜的未评价的
	//1，有故事的
	//2，热点图
	//3，经典图
	public void setState(int state) {
		this.state = state;
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		Rect rect = canvas.getClipBounds();
//        Paint paint = new Paint();
//        paint.setAntiAlias(true);
//        paint.setStyle(Paint.Style.STROKE);
        //这个边框宽度应该合适了
//        paint.setStrokeWidth(3);
        //TODO, 这里的颜色将来要根据数据变化
        //先暂时不画边框了
//        paint.setColor(colorByState());       
//        canvas.drawRect(rect, paint);
	}
	
	//RGB色值参考：http://blog.sina.com.cn/s/blog_49ad23c90100p57h.html
	private int colorByState(){
		
		//默认新鲜出炉的，为淡绿色
		int color = Color.rgb(130, 202, 156);
		
		switch(state){
		
		case 1:
			//有故事的，为天蓝色
			color = Color.rgb(135, 206, 235);
			break;
			
		case 2:
			//热图，为胡萝卜橙色
			color = Color.rgb(240, 133, 25);
			break;
			
		case 3:
			//经典，为珊瑚红
			color = Color.rgb(255, 127, 80);
			break;
					
		}
		return color;
	}

}
