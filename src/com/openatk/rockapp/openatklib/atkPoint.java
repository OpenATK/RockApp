package com.openatk.rockapp.openatklib;

import com.google.android.gms.maps.model.LatLng;

public class atkPoint extends atkModel {
	public LatLng position;
	
	public atkPoint(){
		
	}
	public atkPoint(Object id){
		this.id = id;
	}
	public atkPoint(Object id, LatLng position){
		this.id = id;
		this.position = position;
	}
}
