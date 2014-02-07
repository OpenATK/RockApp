package com.openatk.rockapp.openatklib;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.openatk.rockapp.R;

import pl.mg6.android.maps.extensions.GoogleMap;
import pl.mg6.android.maps.extensions.GoogleMap.OnMapClickListener;
import pl.mg6.android.maps.extensions.GoogleMap.OnMarkerClickListener;
import pl.mg6.android.maps.extensions.Marker;

public class atkMap implements atkTouchableWrapperListener {
	//Callbacks to whoever uses this library
	private atkMapClickListener atkMapClickListener;
	private atkPointClickListener atkPointClickListener;
	private atkPolygonClickListener atkPolygonClickListener;
	private atkPolylineClickListener atkPolylineClickListener;
	private atkDrawListener atkDrawListener;
	
	
	//Static variables, ie. drawing options
	int colorFillPolygonDrawing = Color.argb(100, 191, 0, 136);
	int colorFillCompletePolygonDrawing = Color.argb(200, 200, 200, 200);
	int colorStrokePolygonDrawing = Color.argb(255, 255, 255, 255);
	int colorStrokeCompletePolygonDrawing = Color.argb(255, 0, 0, 0);
	
	int resIdPointSelectedPolylineDrawing = R.drawable.selected_vertex;
	float anchorVPointSelectedPolylineDrawing = 0.5f;
	float anchorUPointSelectedPolylineDrawing = 0.5f;
	float anchorVPanPointSelectedPolylineDrawing = 0.5f;
	float anchorUPanPointSelectedPolylineDrawing = 0.5f;
	int panWidthPointSelectedPolylineDrawing = 64;
	int panHeightPointSelectedPolylineDrawing = 64;
	
	int resIdPointPolylineDrawing = R.drawable.unselected_vertex;
	float anchorVPointPolylineDrawing = 0.5f;
	float anchorUPointPolylineDrawing = 0.5f;
	
	int resIdPointSelectedPolygonDrawing = R.drawable.selected_vertex;
	float anchorVPointSelectedPolygonDrawing = 0.75f;
	float anchorUPointSelectedPolygonDrawing = 0.25f;
	float anchorVPanPointSelectedPolygonDrawing = 0.5f;
	float anchorUPanPointSelectedPolygonDrawing = 0.5f;
	int panWidthPointSelectedPolygonDrawing = 64;
	int panHeightPointSelectedPolygonDrawing = 64;

	int resIdPointPolygonDrawing = R.drawable.unselected_vertex;
	float anchorVPointPolygonDrawing = 0.5f;
	float anchorUPointPolygonDrawing = 0.5f;
	
	

	//Local variables
	BitmapDescriptor iconPointSelectedPolygonDrawing;
	BitmapDescriptor iconPointPolygonDrawing;
	BitmapDescriptor iconPointSelectedPolylineDrawing;
	BitmapDescriptor iconPointPolylineDrawing;
	
	private GoogleMap map;
	private List<atkPointView> points = new ArrayList<atkPointView>();
	private List<atkPolygonView> polygons = new ArrayList<atkPolygonView>();
	private List<atkPolylineView> polylines = new ArrayList<atkPolylineView>();
	
	private int nextPointId = 0;
	
	private boolean isDraggingPoint = false;

	
	private boolean isDrawingPolygon = false;
	private atkPolygonView polygonDrawing;
	private List<atkPointView> pointsPolygonDrawing = new ArrayList<atkPointView>();
	private atkPointView pointSelectedPolygonDrawing;

	private boolean isDrawingPolyline = false;
	private atkPolylineView polylineDrawing;
	private List<atkPointView> pointsPolylineDrawing = new ArrayList<atkPointView>();
	private atkPointView pointSelectedPolylineDrawing;
	
	//Used for dragging
	int panOffsetYPolygonDrawing = 0;
	int panOffsetXPolygonDrawing = 0;
	
	//Used locally to handle events
	private GoogleMapClickListener googleMapClickListener;
	private GoogleMarkerClickListener googleMarkerClickListener;
	
	private Context context;
	private GestureDetector gestureDetector;

	public atkMap(GoogleMap map, Context context){
		this.map = map;
		this.context = context;
		this.googleMapClickListener = new GoogleMapClickListener();
		this.googleMarkerClickListener = new GoogleMarkerClickListener();
		map.setOnMapClickListener(googleMapClickListener);
		map.setOnMarkerClickListener(googleMarkerClickListener);
		
		//Get point icons from resources
		this.iconPointSelectedPolygonDrawing = BitmapDescriptorFactory.fromResource(this.resIdPointSelectedPolygonDrawing);
		this.iconPointPolygonDrawing = BitmapDescriptorFactory.fromResource(this.resIdPointPolygonDrawing);
		this.iconPointSelectedPolylineDrawing = BitmapDescriptorFactory.fromResource(this.resIdPointSelectedPolylineDrawing);
		this.iconPointPolylineDrawing = BitmapDescriptorFactory.fromResource(this.resIdPointPolylineDrawing);
		gestureDetector = new GestureDetector(context, new GestureListener());
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
	
	public atkPointView addPoint(atkPoint point){
		atkPointView pointView = new atkPointView(map,point);
		this.points.add(pointView);
		return pointView;
	}
	public atkPolygonView addPolygon(atkPolygon polygon){
		atkPolygonView polygonView = new atkPolygonView(map, polygon);
		this.polygons.add(polygonView);
		return polygonView;
	}
	
	public boolean updatePoint(atkPoint point){
		if(point.id == null) return false;
		for(int i=0; i<points.size(); i++){
			atkPointView pointView = points.get(i);
			if(pointView.getAtkPoint().id.equals(point.id)){
				pointView.setAtkPoint(point);
				return true;
			}
		}
		return false;
	}
	public boolean updatePolygon(atkPolygon polygon){
		if(polygon.id == null) return false;
		for(int i=0; i<polygons.size(); i++){
			if(polygons.get(i).getAtkPolygon().id.equals(polygon.id)){
				polygons.get(i).setAtkPolygon(polygon);
				return true;
			}
		}
		return false;
	}
	public atkPolygonView getPolygonView(Object atkPolygonId){
		if(atkPolygonId == null) return null;
		for(int i=0; i<polygons.size(); i++){
			if(polygons.get(i).getAtkPolygon().id.equals(atkPolygonId)){
				return polygons.get(i);
			}
		}
		return null;
	}
	
	public atkPolygonView drawPolygon(Object atkPolygonId){
		//atkPolygon id needs to be unique
		//Set to draw mode
		atkPolygon newPoly = new atkPolygon(atkPolygonId);
		if(this.isDrawingPolygon != false) Log.w("atkMap", "Drawing of last polygon was not completed, you have lost that polygon.");
		//Create a the polygon that we are currently drawing
		this.polygonDrawing = new atkPolygonView(map, newPoly);
		this.polygonDrawing.setFillColor(colorFillPolygonDrawing);
		this.polygonDrawing.setStrokeColor(colorStrokePolygonDrawing);
		this.isDrawingPolygon = true;
		return this.polygonDrawing;
	}
	public atkPolygonView completePolygon(){
		//Complete drawing of current polygon
		if(this.isDrawingPolygon == false) return null;
		this.polygonDrawing.setFillColor(colorFillCompletePolygonDrawing);
		this.polygonDrawing.setStrokeColor(colorStrokeCompletePolygonDrawing);
		//Remove all points
		for (Iterator<atkPointView> iter = this.pointsPolygonDrawing.iterator(); iter.hasNext();) {
			atkPointView point = iter.next();
			point.remove(); //Remove from map
			iter.remove(); //Remove from list
		}
		atkPolygonView toReturn = this.polygonDrawing;
		this.polygons.add(this.polygonDrawing);
		this.polygonDrawing = null;
		this.pointSelectedPolygonDrawing = null;
		this.isDrawingPolygon = false;
		return toReturn;
	}
	private class GoogleMarkerClickListener implements OnMarkerClickListener {
		@Override
		public boolean onMarkerClick(Marker marker) {
			//Touched a point, find which one if and if we consumed
			if(isDrawingPolygon == true){
				Log.d("GoogleMarkerClickListener", "isDrawing");
				//We are drawing a polygon
				//Check if we clicked any of its points
				atkPointView clickedPoint = null;
				for(int i=0; i<pointsPolygonDrawing.size(); i++){
					if(pointsPolygonDrawing.get(i).wasClicked(marker) != null){
						clickedPoint = pointsPolygonDrawing.get(i);
						break;
					}
				}
				if(clickedPoint != null){
					//Select this point
					if(pointSelectedPolylineDrawing != null){
						pointSelectedPolylineDrawing.setIcon(iconPointPolylineDrawing);
						pointSelectedPolylineDrawing.setAnchor(anchorUPointPolylineDrawing, anchorVPointPolylineDrawing);
					}
					if(pointSelectedPolygonDrawing != null){
						pointSelectedPolygonDrawing.setIcon(iconPointPolygonDrawing);
						pointSelectedPolygonDrawing.setAnchor(anchorUPointPolygonDrawing, anchorVPointPolygonDrawing);
					}
					pointSelectedPolygonDrawing = clickedPoint;
					clickedPoint.setIcon(iconPointSelectedPolygonDrawing);
					clickedPoint.setAnchor(anchorUPointSelectedPolygonDrawing, anchorVPointSelectedPolygonDrawing);
				}
				return true; //Consume click
			}
			if(isDrawingPolyline == true){
				//We are drawing a polyline
				//Check if we clicked any of its points
				atkPointView clickedPoint = null;
				for(int i=0; i<pointsPolylineDrawing.size(); i++){
					if(pointsPolylineDrawing.get(i).wasClicked(marker) == true){
						clickedPoint = pointsPolylineDrawing.get(i);
						break;
					}
				}
				if(clickedPoint != null){
					//Select this point
					if(pointSelectedPolylineDrawing != null) pointSelectedPolylineDrawing.setIcon(iconPointPolylineDrawing);
					if(pointSelectedPolygonDrawing != null) pointSelectedPolygonDrawing.setIcon(iconPointPolygonDrawing);
					pointSelectedPolylineDrawing = clickedPoint;
					clickedPoint.setIcon(iconPointSelectedPolylineDrawing);
				}
				return true; //Consume click
			}
			
			Boolean wasClicked = null;
			atkPointView point = null;
			for(int i=0; i<points.size(); i++){
				wasClicked = points.get(i).wasClicked(marker); //This does the click event on atkPointClickListener
				if(wasClicked == true){
					return true; //Consume the click
				} else if(wasClicked == false){
					point = points.get(i);
					break;
				}
			}
			if(wasClicked != null){
				//Was clicked but wasn't consumed, pass to default atkPointClickListener
				atkPointClickListener.onClick(point);
			}
			return false;
		}
	}

	private class GoogleMapClickListener implements OnMapClickListener {
		@Override
		public void onMapClick(LatLng position) {
			Log.d("atkMap - GoogleMapClickListener", "onMapClick");
			//Google map was clicked
			if(isDrawingPolygon == true){
				Log.d("atkMap - GoogleMapClickListener", "we are drawing a polygon");
				//We are drawing a polygon
				//Add a point to the map to represent the vertex
				int selectedPointIndex = 0;
				if(pointSelectedPolygonDrawing != null) {
					pointSelectedPolygonDrawing.setIcon(iconPointPolygonDrawing);
					pointSelectedPolygonDrawing.setAnchor(anchorUPointPolygonDrawing, anchorVPointPolygonDrawing);
					selectedPointIndex = pointsPolygonDrawing.indexOf(pointSelectedPolygonDrawing);
				}
				atkPoint point = new atkPoint(nextPointId); //Don't init with position so it won't draw yet
				nextPointId++;
				atkPointView pointView = new atkPointView(map, point);
				pointView.setIcon(iconPointSelectedPolygonDrawing);
				pointView.setAnchor(anchorUPointSelectedPolygonDrawing, anchorVPointSelectedPolygonDrawing);
				point.position = position; //Set position of model
				pointView.update(); //Tell pointView to refresh its view
				
				pointsPolygonDrawing.add(selectedPointIndex, pointView);
				pointSelectedPolygonDrawing = pointView;
				
				//Add a point to the polygon's model that we are currently drawing				
				polygonDrawing.getAtkPolygon().boundary.add(selectedPointIndex, position); //Update its model
				polygonDrawing.update(); //Tell it to refresh its view
			}

			//Check if polygon was clicked
			Boolean polygonClicked = null;
			atkPolygonView polygon = null;
			for(int i=0; i<polygons.size(); i++){
				polygonClicked = polygons.get(i).wasClicked(position);
				if(polygonClicked == true){
					polygon = polygons.get(i);
					break; //We want to consume this click
				} else if(polygonClicked == false){
					polygon = polygons.get(i); //Don't want to consume
					break;
				}
			}

			//Check if polyline was clicked
			Boolean polylineClicked = null;
			atkPolylineView polyline = null;
			for(int i=0; i<polylines.size(); i++){
				//polylineClicked is Null if not clicked, true if it has a clickListener, false otherwise
				polylineClicked = polylines.get(i).wasClicked(position); 
				if(polylineClicked != null){
					polyline = polylines.get(i);
					break;
				}
			}
			
			//If both were clicked find out which we should click
			if(polygonClicked != null && polylineClicked != null){
				//Clicked both, find out which one, default polyline over polygon
				if(polygon.getZIndex() >= polyline.getZIndex()){
					polylineClicked = null; //We didn't clicked the polyline
				} else {
					polygonClicked = null; //We didn't clicked the polygon
				}
			} 
			
			if(polygonClicked != null){
				//Click the polygon and see if the listener consumed it
				boolean consumed = polygon.click();
				if(consumed == false){
					//Pass it to the default listener
					if(atkPolygonClickListener != null) atkPolygonClickListener.onClick(polygon);
				}
				return;
			}
			if(polylineClicked != null){
				//Was clicked but wasn't consumed
				boolean consumed = polyline.click();
				if(consumed == false){
					//Pass it to the default listener
					if(atkPolylineClickListener != null) atkPolylineClickListener.onClick(polyline);
				}
				return;
			}			
			
			//Notify them if we didn't click a polygon or polygon
			if(atkMapClickListener != null) atkMapClickListener.onClick(position);
		}
	}

	@Override
	public boolean onTouch(MotionEvent event) {
		//The map was touched, this triggers before GoogleMapClickListener
		//If we return true GoogleMapClickListener wont get the touch event
		if(this.isDrawingPolygon && isDraggingPoint && event.getActionIndex() == 0 && event.getAction() == MotionEvent.ACTION_MOVE){
			//We are dragging the polygons selected point
			Point thePoint = new Point((int)event.getX() + panOffsetXPolygonDrawing, (int)event.getY() + panOffsetYPolygonDrawing);
						
			this.pointSelectedPolygonDrawing.getAtkPoint().position = map.getProjection().fromScreenLocation(thePoint);
			this.pointSelectedPolygonDrawing.update();
			//TODO streamline this?
			List<LatLng> newBoundary = new ArrayList<LatLng>();
			for(int i=0; i<this.pointsPolygonDrawing.size(); i++){
				newBoundary.add(this.pointsPolygonDrawing.get(i).getAtkPoint().position);
			}
			this.polygonDrawing.getAtkPolygon().boundary = newBoundary;
			this.polygonDrawing.update();
		} else if(this.isDrawingPolygon && isDraggingPoint && event.getActionIndex() == 0 && event.getAction() == MotionEvent.ACTION_UP){
			//Stop dragging polygons selected point
			this.isDraggingPoint = false;
		} else if(this.isDrawingPolygon && event.getActionIndex() == 0 && event.getAction() == MotionEvent.ACTION_DOWN){
			if(this.pointSelectedPolygonDrawing != null){
				//Check if we are over the pan section of this point's icon
				Point markerPoint = map.getProjection().toScreenLocation(this.pointSelectedPolygonDrawing.getAtkPoint().position);
				
				BitmapFactory.Options dimensions = new BitmapFactory.Options(); 
				dimensions.inJustDecodeBounds = true;
				BitmapFactory.decodeResource(this.context.getResources(), this.resIdPointSelectedPolygonDrawing, dimensions);
				int iconHeight = dimensions.outHeight;
				int iconWidth =  dimensions.outWidth;
				
				int y1 = (int) (iconHeight * anchorVPointSelectedPolygonDrawing);
				int x1 = (int) (iconWidth * anchorUPointSelectedPolygonDrawing);
				int y2 = (int) (iconHeight * anchorVPanPointSelectedPolygonDrawing);
				int x2 = (int) (iconWidth * anchorUPanPointSelectedPolygonDrawing);
				
				panOffsetYPolygonDrawing = y1-y2;
				panOffsetXPolygonDrawing = x1-x2;
												
				if((y1-y2 - (panHeightPointSelectedPolygonDrawing/2)) < (markerPoint.y - (int)event.getY())){
					if((y1-y2 + (panHeightPointSelectedPolygonDrawing/2)) > (markerPoint.y - (int)event.getY())){
						if((x1-x2 - (panWidthPointSelectedPolygonDrawing/2)) < (markerPoint.x - (int)event.getX())){
							if((x1-x2 + (panWidthPointSelectedPolygonDrawing/2)) > (markerPoint.x - (int)event.getX())){
								//Move the point and consume the touch event
								isDraggingPoint = true;
								return true;
							}
						}
					}
				}
			}
		}
		
		if(this.isDrawingPolygon){
			if(this.gestureDetector.onTouchEvent(event)){
				//Consume the double tap
				return false;
			}
		}
		
		return false;
	}
	
	 private class GestureListener extends GestureDetector.SimpleOnGestureListener {
		 	private boolean doubleTap = false;
		 		        
	        // event when double tap occurs
	        @Override
	        public boolean onDoubleTap(MotionEvent e) {
	        	Log.d("GestureListener", "double tap");
	        	doubleTap = true;
	            return true;
	        }
	        public boolean wasDoubleTap(){
	        	return doubleTap;
	        }
	    }
}
