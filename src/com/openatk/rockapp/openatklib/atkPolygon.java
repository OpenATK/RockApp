package com.openatk.rockapp.openatklib;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;

public class atkPolygon extends atkModel {
	public List<LatLng> boundary;
	//TODO add holes
	
	public atkPolygon(Object id){
		this.id = id;
		this.boundary = new ArrayList<LatLng>();
	}
	public atkPolygon(Object id, List<LatLng> boundary){
		this.id = id;
		this.boundary = boundary;
	}
}
