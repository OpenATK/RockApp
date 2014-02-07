package com.openatk.rockapp.openatklib;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;

public class atkPolyline extends atkModel {
	public List<LatLng> boundary;
	
	public atkPolyline(Object id){
		this.id = id;
		this.boundary = new ArrayList<LatLng>();
	}
	public atkPolyline(Object id, List<LatLng> boundary){
		this.id = id;
		this.boundary = boundary;
	}
}
