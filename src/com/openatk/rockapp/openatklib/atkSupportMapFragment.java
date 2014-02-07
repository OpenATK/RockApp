package com.openatk.rockapp.openatklib;


import pl.mg6.android.maps.extensions.GoogleMap;
import pl.mg6.android.maps.extensions.SupportMapFragment;

import com.openatk.rockapp.R;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class atkSupportMapFragment extends SupportMapFragment {
	 public View mOriginalContentView;
	 public atkTouchableWrapper mTouchView;   
	 private atkMap map;
	 
	 @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
	    mOriginalContentView = super.onCreateView(inflater, parent, savedInstanceState); 
	    
	    
	    Log.d("atkSupportMapFragment", "onCreateView()");
	    //LayoutInflater vi = (LayoutInflater) this.getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    //View v = vi.inflate(R.layout.pan_button, null);
	    
	    mTouchView = new atkTouchableWrapper(getActivity());
	    mTouchView.addView(mOriginalContentView);
	    //mTouchView.addView(v);
	    
	    //Add listeners for clicks on the map
	    //mTouchView.addListener(new panListener(v));
	    
	    //Would be getMap() if not using android-map-extensions
	    this.map = new atkMap(this.getExtendedMap(), this.getActivity().getApplicationContext()); 
	    mTouchView.addListener(this.map); //Let the atkMap listen for touch events
	    	    
	    return mTouchView;
	 }
	 
	 
	 public atkMap getAtkMap(){
		return this.map;
	 }

	 @Override
	 public View getView() {
		 return mOriginalContentView;
	 }
	 
	 
	 private class panListener implements atkTouchableWrapperListener {

		 private View panButton;

		 public panListener(View panButton){
			 this.panButton = panButton;
		 }

		 @Override
		 public boolean onTouch(MotionEvent event) {
			 Log.d("atkTouchableWrapper", "dispatchTouchEvent");
			 Log.d("atkTouchableWrapper", "event X:"  + Float.toString(event.getX()) + " event Y:" + Float.toString(event.getY()) );
			 Log.d("atkTouchableWrapper", "pic X:"  + Integer.toString(this.panButton.getLeft()) + " pic Y:" + Integer.toString(this.panButton.getTop()) );
			 Log.d("atkTouchableWrapper", "pic wdith:"  + Integer.toString(this.panButton.getWidth()) + " pic height:" + Integer.toString(this.panButton.getHeight()) );

			 //TODO Density stuff
			 float panX = 0.0f;
			 float panY = 0.0f;
			 float panWidth = 0.0f;
			 float panHeight = 0.0f;
			 
			 if(panX < event.getX() && (panX + panWidth) > event.getX()){
				 Log.d("atkTouchableWrapper", "X good");
				 if(panY < event.getY() && (panY + panHeight) > event.getY()){
					 Log.d("atkTouchableWrapper", "Y good");
					 //return true;
				 }
			 }
			 switch (event.getAction()) {
				 case MotionEvent.ACTION_DOWN:
					 break;
				 case MotionEvent.ACTION_UP:
					 break;
			 }
			 return false;
		 }
	 }
	 
}
