package com.pintu.activity;

import com.pintu.R;
import com.pintu.adapter.GalleryImageAdapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.GridView;

public class HomeGallery extends Activity {

	
	
	
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.homegallery);
	
        GridView g = (GridView) findViewById(R.id.ptgallery);
        g.setAdapter(new GalleryImageAdapter(this));

	}
	
	

}
