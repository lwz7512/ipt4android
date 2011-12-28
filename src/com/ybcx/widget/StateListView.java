package com.ybcx.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.AbsListView;
import android.widget.ListView;

public class StateListView extends ListView implements ListView.OnScrollListener {

	  private int scrollState = OnScrollListener.SCROLL_STATE_IDLE;
	
	public StateListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		 setOnScrollListener(this);
	}
	
	//获得当前列表视图的状态，用于解决滑动列表和点击列表中元素动作冲突
	//lwz7512 @ 2011/08/24
	public boolean isStatic() {
		return scrollState==0 ? true : false;
	}

	  @Override
	  public boolean onInterceptTouchEvent(MotionEvent event) {
	    boolean result = super.onInterceptTouchEvent(event);

	    if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
	      return true;
	    }

	    return result;
	  }

	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		this.scrollState = scrollState;	
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		//do nothing currently...		
	}
	

}
