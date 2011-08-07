package com.pintu;

import android.os.Bundle;

import com.pintu.activity.FullScreenActivity;

public class PintuMain extends FullScreenActivity {


	// -------------------- Construction UI Logic
	// -----------------------------------------

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		getViews();

		addEventListeners();

	}



	private void getViews() {

	}

	private void addEventListeners() {

	}


} // end of class