package com.openatk.rockapp.openatklib;

import pl.mg6.android.maps.extensions.GoogleMap;

import com.google.android.gms.maps.model.LatLng;

import android.graphics.Color;

public class atkPolygonView {
	private atkPolygon polygon;
	private GoogleMap map;

	private atkPolygonClickListener clickListener;
	
	public atkPolygonView(){
		
	}
	public atkPolygonView(GoogleMap map, atkPolygon polygon){
		this.map = map;
		this.polygon = polygon;
	}
	
	public void setOnClickListener(atkPolygonClickListener clickListener){
		this.clickListener = clickListener;
	}
	
	public atkPolygon getAtkPolygon(){
		return polygon;
	}
	
	public void update(atkPolygon polygon){
		
	}
	
	public void remove(){
		
	}
	
	public void hide(){
		
	}
	
	public void show(){
		
	}
	
	public void setStrokeColor(Color color){
		
	}
	
	public void setFillColor(Color color){
		
	}

	public void setOpacity(float opacity){
	
	}
	
	public Boolean wasClicked(LatLng point){ //TODO protected?
		//Returns null if wasn't clicked, true or false if clicked depending if we consumed it
		
		//Check if it was clicked
		Boolean consumed = null;
		
		//If clicked pass to clickListener if we have one
		consumed = false;
		if(this.clickListener != null){
			consumed = this.clickListener.onClick(this);
		}
		
		return consumed;
	}
}
