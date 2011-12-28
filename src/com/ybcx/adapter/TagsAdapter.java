package com.ybcx.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ybcx.R;
import com.ybcx.data.Tag;

public class TagsAdapter extends BaseAdapter {

	private Context ctxt;
	private LayoutInflater mInflater;
	
	private List<Tag> tags;
	
	public TagsAdapter(Context c) {
		this.ctxt = c;
		mInflater = LayoutInflater.from(c);
		tags = new ArrayList<Tag>();
	}
	
	@Override
	public int getCount() {
		return tags.size();
	}

	@Override
	public Object getItem(int position) {
		return tags.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		if (convertView == null){
			view = mInflater.inflate(R.layout.tagitem, parent, false);
			ViewHolder holder = new ViewHolder();
			holder.tagCount = (TextView) view.findViewById(R.id.tag_name_count);
			view.setTag(holder);
		}else{
			view = convertView;
		}
		ViewHolder holder = (ViewHolder) view.getTag();
		Tag tag = tags.get(position);
		String tagNameWithCount = tag.name+" ("+tag.browseCount+")";
		holder.tagCount.setText(tagNameWithCount);
		
		return view;
	}
	
	private static class ViewHolder{
		public TextView tagCount;
	}
	
	public void refresh(List<Tag> tags){
		this.tags = tags;
		this.notifyDataSetChanged();
	}

}
