package com.openatk.rockapp.openatklib;

import java.util.List;

import com.google.android.gms.maps.model.LatLng;

import pl.mg6.android.maps.extensions.GoogleMap;
import pl.mg6.android.maps.extensions.GoogleMap.OnMapClickListener;
import pl.mg6.android.maps.extensions.GoogleMap.OnMarkerClickListener;
import pl.mg6.android.maps.extensions.Marker;

public class atkMap  {
	private GoogleMap map;
	private atkMapClickListener atkMapClickListener;
	private atkPointClickListener atkPointClickListener;
	private atkPolygonClickListener atkPolygonClickListener;

	private List<atkPointView> points;
	private List<atkPolygonView> polygons;
	
	private GoogleMapClickListener googleMapClickListener;
	private GoogleMarkerClickListener googleMarkerClickListener;

	public atkMap(GoogleMap map){
		this.map = map;
		
		this.googleMapClickListener = new GoogleMapClickListener();		
		map.setOnMapClickListener(googleMapClickListener);
		map.setOnMarkerClickListener(googleMarkerClickListener);
	}
	
	public void setOnMapClickListener(atkMapClickListener listener){
		this.atkMapClickListener = listener;
	}
	
	public void setOnPointClickListener(atkPointClickListener listener){
		this.atkPointClickListener = listener;
	}
	
	public void setOnPolygonClickListener(atkPolygonClickListener listener){
		this.atkPolygonClickListener = listener;
	}
	
	public atkPolygonView addPolygon(atkPolygon polygon){
		atkPolygonView polygonView = new atkPolygonView(polygon);
		this.polygons.add(polygonView);
		
		return polygonView;
	}

	
	
	
	
	
	private class GoogleMarkerClickListener implements OnMarkerClickListener {
		@Override
		public boolean onMarkerClick(Marker marker) {
			//Touched a point, find which one if and if we consumed
			
			
			
			return false;
		}
	}
	private class GoogleMapClickListener implements OnMapClickListener {
		@Override
		public void onMapClick(LatLng position) {
			//Google map was clicked
			//Check if polygon, or polyline, was clicked
			
			//Notify them
			atkMapClickListener.onClick(position);
		}
	}
}
