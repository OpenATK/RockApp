package com.openatk.rockapp.openatklib;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class atkTouchableWrapper extends FrameLayout {
	
	private List<atkTouchableWrapperListener> listeners;
	public atkTouchableWrapper(Context context) {
		super(context);
		listeners = new ArrayList<atkTouchableWrapperListener>();
	}
	
	public void addListener(atkTouchableWrapperListener listener){
		this.listeners.add(listener);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		for(int i=0; i<listeners.size(); i++){
			if(listeners.get(i).onTouch(event) == true){
				return true; //Touch was consumed
			}
		}		
		return super.dispatchTouchEvent(event);
	}
}
