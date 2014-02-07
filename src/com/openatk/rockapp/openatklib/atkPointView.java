package com.openatk.rockapp.openatklib;

import pl.mg6.android.maps.extensions.GoogleMap;
import pl.mg6.android.maps.extensions.Marker;
import pl.mg6.android.maps.extensions.MarkerOptions;

import com.google.android.gms.maps.model.BitmapDescriptor;

public class atkPointView {
	private atkPoint point;
	private GoogleMap map;
	private atkPointClickListener clickListener;
	
	private Marker marker;
	private MarkerOptions markerOptions;
	

	public atkPointView(GoogleMap map, atkPoint point){
		this.map = map;
		this.point = point;
		markerOptions = new MarkerOptions().position(point.position);
		this.drawPoint();
	}
	
	public atkPoint getAtkPoint(){
		return point;
	}
	public void setAtkPoint(atkPoint point){
		this.point = point;
		this.drawPoint();
	}
	
	public void update(){
		this.drawPoint();
	}
	
	public void remove(){
		if(marker != null) marker.remove();
		marker = null;
	}
	
	public void hide(){
		this.markerOptions.visible(false);
		if(this.marker != null) this.marker.setVisible(false);
	}
	
	public void show(){
		this.markerOptions.visible(true);
		if(this.marker != null) this.marker.setVisible(false);
	}
	
	public void setIcon(BitmapDescriptor icon){
		this.markerOptions.icon(icon);
		if(marker != null) marker.setIcon(icon);
	}
	
	public void setAnchor(float horizontal, float vertical){
		this.markerOptions.anchor(horizontal, vertical);
		if(marker != null) marker.setAnchor(horizontal, vertical);
	}
	
	public void setOnClickListener(atkPointClickListener listener){
		this.clickListener = listener;
	}
	
	public Boolean wasClicked(Marker clickedMarker){  //TODO protected?
		//Returns null if wasn't clicked, false if clicked and consumed, true if clicked and not consumed
		Boolean consumed = null;
		if(this.marker == clickedMarker){
			consumed = false;
			//Check if we have a click listener
			if(this.clickListener != null){
				consumed = this.clickListener.onClick(this);
			}
		}
		return consumed;
	}
	
	private void drawPoint(){
		//Draw the point on the map
		if(point.position != null) {
			markerOptions.position(point.position);
			if(marker == null) {
				marker = this.map.addMarker(markerOptions);
			} else {
				marker.setPosition(point.position);
			}
		}
	}
}
