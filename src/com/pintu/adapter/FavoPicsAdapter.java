package com.pintu.adapter;

import java.util.ArrayList;
import java.util.List;

import com.pintu.data.TPicItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class FavoPicsAdapter extends BaseAdapter {

	private Context ctxt;
	private LayoutInflater mInflater;
	private List<TPicItem> pics;
	
	public FavoPicsAdapter(Context c){
		ctxt = c;
		mInflater = LayoutInflater.from(c);
		pics = new ArrayList<TPicItem>();
	}
	
	@Override
	public int getCount() {
		return pics.size();
	}

	@Override
	public Object getItem(int position) {
		return pics.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return null;
	}

}
