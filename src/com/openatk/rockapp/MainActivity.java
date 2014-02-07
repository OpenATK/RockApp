package com.openatk.rockapp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pl.mg6.android.maps.extensions.ClusteringSettings;
import pl.mg6.android.maps.extensions.GoogleMap;
import pl.mg6.android.maps.extensions.GoogleMap.OnCameraChangeListener;
import pl.mg6.android.maps.extensions.GoogleMap.OnMapClickListener;
import pl.mg6.android.maps.extensions.GoogleMap.OnMarkerClickListener;
import pl.mg6.android.maps.extensions.GoogleMap.OnMarkerDragListener;
import pl.mg6.android.maps.extensions.Marker;
import pl.mg6.android.maps.extensions.SupportMapFragment;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.LatLngBounds.Builder;
import com.openatk.rockapp.db.DatabaseHelper;
import com.openatk.rockapp.models.Rock;
import com.openatk.rockapp.models.Rock.RockListener;
import com.openatk.rockapp.openatklib.atkMap;
import com.openatk.rockapp.openatklib.atkPolygonView;
import com.openatk.rockapp.openatklib.atkSupportMapFragment;
import com.openatk.rockapp.trello.TrelloContentProvider;
import com.openatk.rockapp.R;
import com.openatk.rockapp.RockMenu.RockMenuListener;

public class MainActivity extends FragmentActivity implements
		OnMarkerClickListener, OnMapClickListener, OnMarkerDragListener,
		OnCameraChangeListener, RockMenuListener, RockListener {

	
	private DatabaseHelper dbHelper;

	private GoogleMap map;
	private atkMap atkmap;

	private UiSettings mapSettings;
	private MarkerHandler markerHandler;

	// Startup position
	private static final float START_LAT = 40.428712f;
	private static final float START_LNG = -86.913819f;
	private static final float START_ZOOM = 17.0f;
	
    public static final String INTENT_ROCKS_UPDATED = "com.openatk.rockapp.rocks.UPDATED"; //TODO put this in shared lib

	// UI States
	private int mCurrentState;
	private int mCurrentRockSelected;

	private static final int STATE_DEFAULT = 0;
	private static final int STATE_ROCK_EDIT = 1;

	// UI Rock View (Picked/Not Picked/Both) States
	private int mRockState;
	public static final int STATE_ROCKS_PICKED_UP = 0;
	public static final int STATE_ROCKS_NOT_PICKED_UP = 1;
	public static final int STATE_ROCKS_BOTH = 2;

	// Slide up menu
	RockMenu slideMenu = null;
	private MapBroadcastReciever mapBroadcastReciever;

	// Request codes for activity results
	private static final int REQUEST_PICTURE = 1;

	// Broadcast actions
	public static final String ACTION_UPDATE_MAP = "com.openatk.rockapp.UPDATE_MAP";

	private List<LatLng> undoMoves = new ArrayList<LatLng>();
	private LatLng dragPosStart;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		dbHelper = new DatabaseHelper(this);
		
		FragmentManager fm = getSupportFragmentManager();
		atkSupportMapFragment f = (atkSupportMapFragment) fm.findFragmentById(R.id.map);

		if (savedInstanceState == null) {
			// First incarnation of this activity.
			f.setRetainInstance(true);
		} else {
			// Reincarnated activity. The obtained map is the same map instance
			// in the previous
			// activity life cycle. There is no need to reinitialize it.
			map = f.getExtendedMap();
			atkmap = f.getAtkMap();
		}
		
		checkGPS();

		mCurrentRockSelected = Rock.BLANK_ROCK_ID;

		slideMenu = (RockMenu) findViewById(R.id.rock_edit_layout);
		slideMenu.setDBHelper(dbHelper);
		slideMenu.setListener(this);
		
		mapBroadcastReciever = new MapBroadcastReciever();

		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		mRockState = prefs.getInt("mRockState", STATE_ROCKS_NOT_PICKED_UP);
		// Restore state from savedInstanceState
		if (savedInstanceState != null) {
			mCurrentRockSelected = savedInstanceState.getInt("rock_edit.currentRock", Rock.BLANK_ROCK_ID);
			
			switch (savedInstanceState.getInt("state", STATE_DEFAULT)) {
				case STATE_ROCK_EDIT:
					// Get RockId and restore view states for setState()
					Log.d("MainActivity", "Editing rock:" + Integer.toString(mCurrentRockSelected));
					SQLiteDatabase database = dbHelper.getReadableDatabase();
					Rock theRock = Rock.getRockById(database, mCurrentRockSelected);
					database.close();
					dbHelper.close();
					
					theRock.setListener(this);
					slideMenu.editRock(theRock);
					break;
			}
			Log.d("MainActivity", "Startup State:" + Integer.toString(savedInstanceState.getInt("state", STATE_DEFAULT)));
			// Restore previous state
			setState(savedInstanceState.getInt("state", STATE_DEFAULT));
		} else {
			// Otherwise set default initial state
			setState(STATE_DEFAULT);
			// TODO Zoom all markers (rocks)
			mRockState = STATE_ROCKS_NOT_PICKED_UP;
		}
		setUpMapIfNeeded();
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putInt("state", mCurrentState);
		savedInstanceState.putInt("rock_edit.currentRock", mCurrentRockSelected);
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("MainActivity", "Resume");

		checkGPS();
		// Listen for image requests from RockMenu
		LocalBroadcastManager.getInstance(this).registerReceiver(mapBroadcastReciever, new IntentFilter(MainActivity.INTENT_ROCKS_UPDATED));
		markerHandler.populateMap(mCurrentRockSelected);
	}

	@Override
	protected void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mapBroadcastReciever);

		// Save location
		CameraPosition myCam = map.getCameraPosition();
		if(myCam != null){
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			SharedPreferences.Editor editor = prefs.edit();
			LatLng where = myCam.target;
			editor.putFloat("StartupLat", (float) where.latitude);
			editor.putFloat("StartupLng",(float) where.longitude); 
			editor.putFloat("StartupZoom",(float) map.getCameraPosition().zoom); 
			editor.commit();
		}
	}

	private void setUpMapIfNeeded() {
		if (map == null) {
			map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getExtendedMap();
			atkmap = ((atkSupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getAtkMap();
		}
		markerHandler = new MarkerHandler(this, map, dbHelper, mCurrentRockSelected);
		slideMenu.setMarkerHandler(markerHandler);
		if (map != null) {
			setUpMap();
			map.setOnMarkerDragListener(this);
			//map.setOnMarkerClickListener(this); TODO
			//map.setOnMapClickListener(this); TODO
			map.setOnCameraChangeListener(this);
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			Float startLat = prefs.getFloat("StartupLat", START_LAT);
			Float startLng = prefs.getFloat("StartupLng", START_LNG);
			Float startZoom = prefs.getFloat("StartupZoom", START_ZOOM);
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(startLat, startLng), startZoom));
		}
	}

	private void setUpMap() {
		mapSettings = map.getUiSettings();
		mapSettings.setZoomControlsEnabled(false);
		mapSettings.setMyLocationButtonEnabled(false);
		mapSettings.setTiltGesturesEnabled(false);
		map.setMyLocationEnabled(true);
		map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		ClusteringSettings clusteringSettings = new ClusteringSettings();
		clusteringSettings.clusterOptionsProvider(new MyClusterOptionsProvider(getResources()));
		clusteringSettings.addMarkersDynamically(true);
		clusteringSettings.clusterSize(96.0);
		clusteringSettings.enabled(true);
		map.setClustering(clusteringSettings);
		markerHandler.setRockState(mRockState);
		markerHandler.populateMap(mCurrentRockSelected);
	}

	/*
	 * Creates the ActionBar with the main menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/*
	 * A method which is called by Android to give the app the change to modify
	 * the menu. Call when context menu is display or after a
	 * invalidateOptionsMenu() for the ActionBar
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		if (mCurrentState == STATE_DEFAULT) {
			menu.clear();
			inflater.inflate(R.menu.main, menu);

			MenuItem showHideItem = menu.findItem(R.id.show_hide);
			MenuItem currentShowHideItem;

			switch (mRockState) {
			case STATE_ROCKS_BOTH:
				currentShowHideItem = menu.findItem(R.id.all_rocks);
				break;

			case STATE_ROCKS_NOT_PICKED_UP:
				currentShowHideItem = menu.findItem(R.id.not_picked_rocks);
				break;

			case STATE_ROCKS_PICKED_UP:
				currentShowHideItem = menu.findItem(R.id.picked_rocks);
				break;
			default:
				// We are some how lost, just revert back to showing everything
				Log.e("MainActivity", "Lost Rock Shown State");
				mRockState = STATE_ROCKS_BOTH;
				currentShowHideItem = menu.findItem(R.id.all_rocks);
				break;
			}

			// Copy the current selection to the action bar
			showHideItem.setTitle(currentShowHideItem.getTitle());

			// Mark the current one as checked
			currentShowHideItem.setChecked(true);

			// The location button changes depending the current state
			// of location
			
			// TODO
			/*
			 * MenuItem gps = menu.findItem(R.id.gps); if(map.ha) {
			 * if(mRockLocationManager.haveUserLocation()) {
			 * gps.setIcon(R.drawable.gps_found);
			 * gps.setTitle(R.string.menu_gps); } else {
			 * gps.setIcon(R.drawable.gps_searching);
			 * gps.setTitle(R.string.menu_gps_searching); } } else {
			 * gps.setIcon(R.drawable.gps_off);
			 * gps.setTitle(R.string.menu_gps_off); }
			 */
		} else if (mCurrentState == STATE_ROCK_EDIT) {
			menu.clear();
			inflater.inflate(R.menu.rock_edit_menu, menu);
			MenuItem undoButton = menu.findItem(R.id.rock_undo_move);
			if (undoMoves.isEmpty()) {
				undoButton.setVisible(false);
			} else {
				undoButton.setVisible(true);
			}
		}
		return true;
	}

	/*
	 * Handles when a user selects an ActionBar menu options
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = false;

		switch (item.getItemId()) {
		case R.id.add:
			addRock();
			result = true;
			break;

		case R.id.gps:
			Location myLoc = map.getMyLocation();
			if (myLoc == null) {
				Toast.makeText(this, R.string.location_wait, Toast.LENGTH_SHORT).show();
			} else {
				CameraPosition oldPos = map.getCameraPosition();
				CameraPosition newPos = new CameraPosition(new LatLng(
						myLoc.getLatitude(), myLoc.getLongitude()),
						map.getMaxZoomLevel(), oldPos.tilt, oldPos.bearing);
				map.animateCamera(CameraUpdateFactory.newCameraPosition(newPos));
			}
			result = true;
			break;

		case R.id.all_rocks:
			// set the new showHide, update the map, and update the action bar
			changeRockTypeShowHide(STATE_ROCKS_BOTH);
			result = true;
			break;

		case R.id.not_picked_rocks:
			// set the new showHide, update the map, and update the action bar
			changeRockTypeShowHide(STATE_ROCKS_NOT_PICKED_UP);
			result = true;
			break;

		case R.id.picked_rocks:
			// set the new showHide, update the map, and update the action bar
			changeRockTypeShowHide(STATE_ROCKS_PICKED_UP);
			result = true;
			break;

		case R.id.list:
			// showRockList();
			if(atkmap == null){
				Log.d("MainActivity", "atkmap is null");
			}
			atkPolygonView polygonView = atkmap.drawPolygon(0);
			
			result = true;
			break;
		case R.id.rock_delete:
			showConfirmRockDeleteAlert();
			result = true;
			break;

		case R.id.rock_undo_move:
			undoRockMove();
			result = true;
			break;

		case R.id.sync:
			//Tell trello app to sync
			TrelloContentProvider.Sync(this.getApplicationContext().getPackageName());
			result = true;
			break;
		case R.id.menu_help:
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
	        alert.setTitle("Help");
	        WebView wv = new WebView(this);
	        wv.loadUrl("file:///android_asset/Help.html");
	        wv.setWebViewClient(new WebViewClient()
	        {
	            @Override
	            public boolean shouldOverrideUrlLoading(WebView view, String url)
	            {
	                view.loadUrl(url);
	                return true;
	            }
	        });
	        alert.setView(wv);
	        alert.setNegativeButton("Close", null);
	        alert.show();
	        break;
		case R.id.menu_legal:
			CharSequence licence= "The MIT License (MIT)\n" +
	                "\n" +
	                "Copyright (c) 2013 Purdue University\n" +
	                "\n" +
	                "Permission is hereby granted, free of charge, to any person obtaining a copy " +
	                "of this software and associated documentation files (the \"Software\"), to deal " +
	                "in the Software without restriction, including without limitation the rights " +
	                "to use, copy, modify, merge, publish, distribute, sublicense, and/or sell " +
	                "copies of the Software, and to permit persons to whom the Software is " +
	                "furnished to do so, subject to the following conditions:" +
	                "\n" +
	                "The above copyright notice and this permission notice shall be included in " +
	                "all copies or substantial portions of the Software.\n" +
	                "\n" +
	                "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR " +
	                "IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, " +
	                "FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE " +
	                "AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER " +
	                "LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, " +
	                "OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN " +
	                "THE SOFTWARE.\n";
			new AlertDialog.Builder(this)
				.setTitle("Legal")
				.setMessage(licence)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton("Close", null).show();
			break;
		}

		// If we didn't handle, let the super version try
		return result | super.onOptionsItemSelected(item);
	}

	/*
	 * A helper function to set what rock type to show/hide
	 */
	private void changeRockTypeShowHide(int type) {
		// set the new showHide, update the map, and update the action bar
		mRockState = type;
		markerHandler.setRockState(mRockState);
		markerHandler.populateMap(mCurrentRockSelected);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("mRockState", type);
		editor.commit();
		
		this.invalidateOptionsMenu();
	}

	private void addRock() {
		Location myLoc = map.getMyLocation();
		LatLng where = null;
		if (myLoc == null) {
			where = map.getCameraPosition().target;
		} else {
			// See if location is on screen
			LatLng myLatLng = new LatLng(myLoc.getLatitude(),
					myLoc.getLongitude());
			if (map.getProjection().getVisibleRegion().latLngBounds
					.contains(myLatLng)) {
				// Location is on screen set marker there
				where = myLatLng;
			} else {
				// Location is off screen, use center of screen
				where = map.getCameraPosition().target;
			}
		}
		Log.d("MainActivity", "Add rock");
		// Rock and save in DB (triggering it to display on the map)
		Rock rock = new Rock(this, where.latitude, where.longitude, false);
		rock.setTrelloId("");
		rock.setHasSynced(false);
		rock.setPosChanged(new Date());
		
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		rock.save(database);
		database.close();
		dbHelper.close();
		

		slideMenu.editRock(rock);
		setState(STATE_ROCK_EDIT);
		selectRock(rock.getId());
	}

	/*
	 * A helper function which knows how to transition between states of the
	 * views
	 */
	private void setState(int newState) {
		if (mCurrentState == newState) {
			return;
		}
		// Exit current state
		switch (mCurrentState) {
		case STATE_ROCK_EDIT:
			slideMenu
					.hide((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE));
			break;
		}

		// Enter new state
		switch (newState) {
		case STATE_DEFAULT:
			if (slideMenu.isOpen()) {
				slideMenu.hide();
			}
			break;

		case STATE_ROCK_EDIT:
			slideMenu.show();
			break;
		}

		// Officially in new state
		mCurrentState = newState;
		this.invalidateOptionsMenu();
	}

	/*
	 * Listen for requests for map to update data
	 */
	private class MapBroadcastReciever extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction() == MainActivity.INTENT_ROCKS_UPDATED) {
				Log.d("MainActivty", "INTENT_ROCKS_UPDATED");
				markerHandler.populateMap(mCurrentRockSelected);
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d("MainActivity", "onActivityResult");
		switch (requestCode) {
			// Get the result of taking a picture
			case REQUEST_PICTURE:
				if (resultCode == RESULT_OK) {
					Marker marker = markerHandler.getMarkerByRockId(mCurrentRockSelected);
					if (marker != null) {
						Rock rock = (Rock) marker.getData();
						if (rock != null) {
							// Update the rock model and save it
							File image = new File(Rock.IMAGE_PATH, String.format(Rock.IMAGE_FILENAME_PATTERN, rock.getId()));
							rock.setPicture(image.getAbsolutePath());
							
							// TODO set changed and changed date to upload the rock photo
							SQLiteDatabase database = dbHelper.getWritableDatabase();
							rock.save(database);
							database.close();
							dbHelper.close();
							slideMenu.editRock(rock);
						}
					}
				}
				break;
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		// When a marker is clicked, return true to prevent info window and
		// centering on marker
		if (marker.isCluster()) {
			// TODO check if max zoom, if so bring up select dialog
			if (map.getCameraPosition().zoom == map.getMaxZoomLevel()) {
				List<Marker> markers = marker.getMarkers();
				// Select one of the markers
				Marker theMarker = markers.get(0);

				Rock theRock = (Rock) theMarker.getData();
				if (theRock == null) {
					Log.e("MainActivity",
							"Clicked marker with null marker data");
				} else {
					selectRock(theRock.getId());
					slideMenu.editRock(theRock);
					setState(STATE_ROCK_EDIT);
				}
			} else {
				List<Marker> markers = marker.getMarkers();
				Builder builder = LatLngBounds.builder();
				for (Marker m : markers) {
					builder.include(m.getPosition());
				}
				LatLngBounds bounds = builder.build();
				map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,
						getResources().getDimensionPixelSize(R.dimen.padding)));
			}

		} else {
			// Single marker, bring up edit screen (slideMenu)
			Rock theRock = (Rock) marker.getData();
			if (theRock == null) {
				Log.e("MainActivity", "Clicked marker with null marker data");
			} else {
				selectRock(theRock.getId());
				slideMenu.editRock(theRock);
				setState(STATE_ROCK_EDIT);
			}
		}
		return true;
	}

	@Override
	public void onMapClick(LatLng position) {
		Log.d("MainActivty", "Map Clicked");
		// If editing then close edit
		if (mCurrentState == STATE_ROCK_EDIT) {
			setState(STATE_DEFAULT);
		}
		// Unselect rock
		selectRock(Rock.BLANK_ROCK_ID);
	}

	public void selectRock(int theRockId) {
		if (mCurrentRockSelected != theRockId) {
			undoMoves.clear(); // Clear last undos
			mCurrentRockSelected = theRockId;
			markerHandler.selectMarker(theRockId);
		}
	}

	/*
	 * Creates a dialog which the user can use to confirm a rock delete
	 */
	private void showConfirmRockDeleteAlert() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(R.string.rock_delete);
		builder.setMessage(R.string.rock_delete_title);

		builder.setPositiveButton(R.string.rock_delete_yes,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// Delete the rock that is currently being edited
						slideMenu.delete();
						setState(STATE_DEFAULT);
						// Unselect rock
						selectRock(Rock.BLANK_ROCK_ID);
					}
				});

		builder.setNegativeButton(R.string.rock_delete_no,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// Do nothing... The app is much less interesting...
					}
				});

		builder.create().show();
	}

	@Override
	public void onMarkerDragStart(Marker marker) {
		// Save original location
		Log.d("MainActivity", "Start drag");
		Rock mRock = (Rock) marker.getData();
		undoMoves.add(0, new LatLng(mRock.getLat(), mRock.getLon()));
		dragPosStart = marker.getPosition();

		Log.d("MainActivity", "MarkerLat:" + Double.toString(marker.getPosition().latitude));
		Log.d("MainActivity", "RockLat:" + Double.toString(mRock.getLat()));
		Log.d("MainActivity", "MarkerLng:" + Double.toString(marker.getPosition().longitude));
		Log.d("MainActivity", "RockLng:" + Double.toString(mRock.getLon()));
	}

	@Override
	public void onMarkerDrag(Marker marker) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		// Show undo option in ActionBar
		Log.d("MainActivity", "Stop drag");

		Rock mRock = (Rock) marker.getData();
		Double newLat = mRock.getLat() + marker.getPosition().latitude - dragPosStart.latitude;
		Double newLng = mRock.getLon() + marker.getPosition().longitude - dragPosStart.longitude;

		// Update marker's data
		mRock.setLat(newLat);
		mRock.setLon(newLng);
		marker.setData(mRock);

		marker.setPosition(new LatLng(newLat, newLng));
		slideMenu.setNewLatLng(new LatLng(newLat, newLng));
		if (undoMoves.size() == 1) {
			// Just changed from empty
			this.invalidateOptionsMenu();
		}
	}

	private void undoRockMove() {
		if (mCurrentRockSelected != Rock.BLANK_ROCK_ID
				&& undoMoves.isEmpty() == false) {
			Marker theMarker = markerHandler.getMarkerByRockId(mCurrentRockSelected);
			if (theMarker != null) {
				LatLng lastPos = undoMoves.remove(0);
				theMarker.setPosition(lastPos);
				Rock mRock = (Rock) theMarker.getData();
				mRock.setLat(lastPos.latitude);
				mRock.setLon(lastPos.longitude);
				theMarker.setData(mRock);
				slideMenu.setNewLatLng(lastPos);
			}
		}
		if (undoMoves.isEmpty()) {
			// Hide undo option in ActionBar
			this.invalidateOptionsMenu();
		}
	}

	@Override
	public void onCameraChange(CameraPosition cameraPosition) {
		if (map.getMaxZoomLevel() == cameraPosition.zoom) {
			// Turn off clustering
			ClusteringSettings clusteringSettings = new ClusteringSettings();
			clusteringSettings.clusterOptionsProvider(new MyClusterOptionsProvider(getResources()));
			clusteringSettings.addMarkersDynamically(true);
			clusteringSettings.clusterSize(96.0);
			clusteringSettings.enabled(false);
			map.setClustering(clusteringSettings);
		} else {
			ClusteringSettings clusteringSettings = new ClusteringSettings();
			clusteringSettings.clusterOptionsProvider(new MyClusterOptionsProvider(getResources()));
			clusteringSettings.addMarkersDynamically(true);
			clusteringSettings.clusterSize(96.0);
			clusteringSettings.enabled(true);
			map.setClustering(clusteringSettings);
		}
	}
	private void checkGPS(){
		final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
	    if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
	        buildAlertMessageNoGps();
	    }
	}
	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
		               startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
		                dialog.cancel();
		           }
		       });
		final AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	public void RockTakePicture(int id, String filepath, String filename) {
		// Get the new image path
		File path = new File(filepath);
		path.mkdirs();
		File image = new File(path, filename);
		try {
			image.createNewFile();
		} catch (IOException e) {
			Log.w("RockAppActivity", "Could not make file for image. "
					+ image.getAbsolutePath() + " " + e.toString());
			return;
		}

		// Put together image capture intent
		Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		takePic.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));

		// Fire intent
		startActivityForResult(Intent.createChooser(takePic, "Capture Image"), REQUEST_PICTURE);
	}

	@Override
	public void RockPopulateMap(Rock theRock) {
		//Add rock on screen or change it
		markerHandler.populateMap(mCurrentRockSelected);
	}
}
