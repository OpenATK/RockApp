package com.openatk.rockapp.openatklib;

import pl.mg6.android.maps.extensions.GoogleMap;

import com.google.android.gms.maps.model.LatLng;

import android.graphics.Color;

public class atkPolylineView {
	private atkPolyline polyline;
	private GoogleMap map;
	private float zindex;
	
	private atkPolylineClickListener clickListener;
	
	public atkPolylineView(GoogleMap map, atkPolyline polyline){
		this.map = map;
		this.polyline = polyline;
	}
	
	public void setOnClickListener(atkPolylineClickListener clickListener){
		this.clickListener = clickListener;
	}
	
	public atkPolyline getAtkPolyline(){
		return polyline;
	}
	
	public void update(){
		
	}
	
	public void remove(){
		
	}
	
	public void hide(){
		
	}
	
	public void show(){
		
	}
	
	public void setColor(Color color){
		
	}
	
	public void setStrokeWidth(float width){
		
	}
	
	public void setZIndex(float zindex){
		this.zindex = zindex;
	}
	
	public float getZIndex(){
		return this.zindex;
	}
	
	public Boolean wasClicked(LatLng point){ //TODO protected?
		//Returns null if wasn't clicked, true or false if clicked depending if we consumed it		
		Boolean couldConsume = null;
		//TODO check if it was clicked
		if(false){
			couldConsume = false;
			if(this.clickListener != null){
				//Notify that we might this click event
				couldConsume = true;
			}
		}
		return couldConsume;
	}
	
	public boolean click(){ //TODO protected?
		//Returns true or false depending if listener consumed the click event		
		if(this.clickListener != null){
			return this.clickListener.onClick(this); //Return if we consumed the click
		}
		return false;
	}
}
