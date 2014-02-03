package com.openatk.rockapp.openatklib;

import java.util.List;

import com.google.android.gms.maps.model.LatLng;

public class atkPolygon extends atkModel {
	public List<LatLng> boundary;
	//TODO add holes
	
	public atkPolygon(){
		
	}
	public atkPolygon(Object id){
		this.id = id;
	}
	public atkPolygon(Object id, List<LatLng> boundary){
		this.id = id;
		this.boundary = boundary;
	}
}
