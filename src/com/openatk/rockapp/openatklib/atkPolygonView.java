package com.openatk.rockapp.openatklib;


import pl.mg6.android.maps.extensions.GoogleMap;
import pl.mg6.android.maps.extensions.Polygon;
import pl.mg6.android.maps.extensions.PolygonOptions;

import com.google.android.gms.maps.model.LatLng;

import android.graphics.Color;
import android.util.Log;

public class atkPolygonView {
	private atkPolygon polygon;
	private GoogleMap map;
	
	
	private Polygon mapPolygon;
	private PolygonOptions polygonOptions;
	private atkPolygonClickListener clickListener;
	
	private int strokeColor = Color.argb(150, 150, 150, 150);
	private int fillColor = Color.argb(200, 200, 200, 200);
	private float strokeWidth = 3.0f;
	private boolean visible = true;
	private float zindex = 1.0f;
	
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
	
	public void setAtkPolygon(atkPolygon polygon){
		this.polygon = polygon; //If the whole model changed
		this.drawPolygon();
	}
	
	public void update(){
		this.drawPolygon();
	}
	
	public void remove(){
		this.mapPolygon.remove();
		this.mapPolygon = null;
	}
	
	public void hide(){
		this.visible = false;
		if(this.mapPolygon != null){
			this.mapPolygon.setVisible(false);
		}
	}
	
	public void show(){
		this.visible = false;
		if(this.mapPolygon != null){
			this.mapPolygon.setVisible(true);
		}
	}
	
	public void setStrokeColor(int color){
		this.strokeColor = color;
		if(this.mapPolygon != null) this.mapPolygon.setStrokeColor(this.strokeColor);
	}
	
	public void setStrokeColor(float alpha, int red, int green, int blue){
		this.strokeColor = Color.argb((int)(alpha * 255),  red, green, blue);
		if(this.mapPolygon != null) this.mapPolygon.setStrokeColor(this.strokeColor);
	}
	
	public void setFillColor(int color){
		this.fillColor = color;
		if(this.mapPolygon != null) this.mapPolygon.setFillColor(this.fillColor);
	}
	
	public void setFillColor(float alpha, int red, int green, int blue){
		this.fillColor = Color.argb((int)(alpha * 255),  red, green, blue);
		if(this.mapPolygon != null) this.mapPolygon.setFillColor(this.fillColor);
	}
	
	public void setStrokeWidth(float width){
		this.strokeWidth = width;
		if(this.mapPolygon != null) this.mapPolygon.setStrokeWidth(this.strokeWidth);
	}

	public void setOpacity(float opacity){
		this.fillColor = Color.argb((int)(opacity * 255), Color.red(this.fillColor), Color.green(this.fillColor), Color.blue(this.fillColor));
		if(this.mapPolygon != null) this.mapPolygon.setFillColor(this.fillColor);
	}
	
	public void setZIndex(float zindex){
		this.zindex = zindex;
		if(this.mapPolygon != null) this.mapPolygon.setZIndex(this.zindex );
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
		
	private void drawPolygon(){
		Log.d("atkPolygonView", "drawPolygon");
		if(this.polygon.boundary != null && this.polygon.boundary.size() > 0){
			if(this.mapPolygon == null){
				Log.d("atkPolygonView", "Creating polygon");
				//Setup options
				this.polygonOptions = new PolygonOptions();			
				this.polygonOptions.addAll(polygon.boundary);
				//this.polygonOptions.strokeColor(this.strokeColor);
				this.polygonOptions.strokeWidth(this.strokeWidth);
				this.polygonOptions.fillColor(this.fillColor);
				//this.polygonOptions.visible(this.visible);
				//this.polygonOptions.zIndex(this.zindex);
				this.mapPolygon = map.addPolygon(this.polygonOptions);
			} else {
				Log.d("atkPolygonView", "Updating # points:" + Integer.toString(this.polygon.boundary.size()));
				this.mapPolygon.setPoints(this.polygon.boundary);
			}
		} else {
			Log.d("atkPolygonView", "removing");
			//Model doesn't have a boundary remove the polygon from the map
			if(this.mapPolygon != null) this.mapPolygon.remove();
			this.mapPolygon = null;
		}
	}
}
