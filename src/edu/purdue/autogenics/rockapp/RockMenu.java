package edu.purdue.autogenics.rockapp;

import java.io.File;

import com.google.android.gms.maps.model.LatLng;

import pl.mg6.android.maps.extensions.Marker;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import edu.purdue.autogenics.libcommon.rock.Rock;
import edu.purdue.autogenics.libcommon.utils.ImageTools;
import edu.purdue.autogenics.libcommon.view.SlideLayout;
import android.view.View.OnClickListener;


public class RockMenu extends SlideLayout implements OnClickListener {
	private EditText comments;
	private ImageButton picked;
	private ImageButton picture;
	private Drawable pictureDrawable;
	private Rock rock = null;
	private Context context;
	private MarkerHandler markerHandler;
	
	private LatLng newLatLng = null;

	private RockBroadcastReceiver rockBroadcastReceiver;
	private ActionMode editActionMode;
	private ActionMode imageActionMode;	
	
	// Broadcast actions
	public static final String ACTION_TAKE_PICTURE = "edu.purdue.autogenics.rockapp.rockmenu.TAKE_PICTURE";
	
	public RockMenu(final Context context, AttributeSet attrs) {
		super(context, attrs);
		
		this.context = context; 
		
		// Start off not editing a rock
		rock = null;
	}
	
	public void setMarkerHandler(MarkerHandler theMarkerHandler){
		markerHandler = theMarkerHandler;
	}
	
	/*
	 * Have to wait for children to inflate before getting pointers to them
	 */
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		// Get a hold of the view
		comments = (EditText)findViewById(R.id.comments);
		picked = (ImageButton)findViewById(R.id.button_picked);
		picture = (ImageButton)findViewById(R.id.button_picture);
		pictureDrawable = context.getResources().getDrawable(R.drawable.rock_picture);
		
		// Listen for changes to the text box so they can be saved to the rock
		comments.setOnFocusChangeListener(new CommentOnFocusChangeListener());
		
		// Listen for clicks/long holds on the picture button
		picture.setOnClickListener(this);
		picture.setOnLongClickListener(new PictureOnLongClickListener());
		
		// Listen for clicks on picked button
		picked.setOnClickListener(this);
	}

	/*
	 * Overload of original hide() which will also close the on screen keyboard
	 */
	public void hide(InputMethodManager imm) {
		super.hide();
		
		imm.hideSoftInputFromWindow(getWindowToken(), 0);
		
		// Remove action mode from rock edit if it exists
		if(editActionMode != null) {
			editActionMode.finish();
		}
		
		// Remove action mode from image if it exists
		if(imageActionMode != null) {
			imageActionMode.finish();
		}
	}
	
	/*
	 * Transitions the edit view to a new model rock
	 */
	public void editRock(Rock rock) {
		// Flush changes from last rock
		this.flush();		
		
		// Store the new rock 
		this.rock = rock;
		
		// Update the view to the new model
		updateMenu();
	}
	
	public void setNewLatLng(LatLng position){
		newLatLng = position;
	}
	/*
	 * Update the view from the current rock model
	 */
	private void updateMenu() {
		if(rock == null) {
			return;
		}
		
		comments.setText(rock.getComments());
		
		if(rock.isPicked()) {
			picked.setSelected(true);
		} else {
			picked.setSelected(false);
		}
		
		String imagePath = rock.getPicture();
		if(imagePath != null) {
			picture.setImageBitmap(ImageTools.sizePicture(imagePath, pictureDrawable.getIntrinsicHeight(), pictureDrawable.getIntrinsicWidth()));
		} else {
			picture.setImageDrawable(pictureDrawable);
		}
	}
	
	public void delete() {
		this.rock.delete();
		this.rock = null;
	}
	
	/*
	 * Check to see if the conditions to save the rock hold true
	 * And if so, then save the rock
	 */
	public void flush() {
		if(this.rock != null) {
			boolean needSave = false;
			
			// Compare and set comments if different
			String rockComment = this.rock.getComments();
			String commentsComment = comments.getText().toString();
			
			if( (rockComment != null && commentsComment.compareTo(rockComment) != 0) ||
				(rockComment == null && commentsComment.length() > 0) ){
				rock.setComments(commentsComment);
				needSave = true;
			}
			
			if(newLatLng != null){
				Log.d("RockMenu", "TEST");
				this.rock.setLon(newLatLng.longitude);
				this.rock.setLat(newLatLng.latitude);
				needSave = true;
				Log.d("RockMenu", "Rock position has changed.");
			}
			
			
			// Compare and set picked status if different
			if(picked.isSelected() != this.rock.isPicked()) {
				this.rock.setPicked(picked.isSelected());
				markerHandler.changeMarkerIcon(rock);
				needSave = true;
			}
			
			// If something was changed then we need to save
			if(needSave) {
				this.rock.setChanged(true);
				this.rock.save();
			}			
			this.newLatLng = null;
			this.rock = null;
		}
	}
	
	/*
	 * A method to register listeners (should be called by activity in onResume)
	 */
	public void registerListeners() {
		if(rockBroadcastReceiver == null) {
			rockBroadcastReceiver = new RockBroadcastReceiver();
			LocalBroadcastManager.getInstance(context).registerReceiver(rockBroadcastReceiver, new IntentFilter(Rock.ACTION_UPDATED));
			LocalBroadcastManager.getInstance(context).registerReceiver(rockBroadcastReceiver, new IntentFilter(Rock.ACTION_LONG_HOLD));
			
			//Don't need?
			//LocalBroadcastManager.getInstance(context).registerReceiver(rockBroadcastReceiver, new IntentFilter(Rock.ACTION_DELETED));
		}
	}
	
	/*
	 * A method to unregister listeners (should be called by activity in onPause)
	 */
	public void unregisterListeners() {
		if(rockBroadcastReceiver != null) {
			LocalBroadcastManager.getInstance(context).unregisterReceiver(rockBroadcastReceiver);
			rockBroadcastReceiver = null;
		}
	}
	
	/*
	 * Focus Change Listener which watches for possible changes in the rocks comments
	 */
	private class CommentOnFocusChangeListener implements OnFocusChangeListener {

		public void onFocusChange(View v, boolean hasFocus) {
			if(!hasFocus) {
				if(RockMenu.this.rock != null){
					RockMenu.this.rock.setComments(comments.getText().toString());
					RockMenu.this.rock.setChanged(true);
					RockMenu.this.rock.save();
				}
			}
		}
	}
	
	
	/*
	 * Listener for long holds on the picture button
	 */
	private class PictureOnLongClickListener implements OnLongClickListener {

		public boolean onLongClick(View v) {
			imageActionMode = startActionMode(new RockImageActionModeCallback());
			return true;
		}
	}
	
	
	/*
	 * Listen on changes to rocks and react to the current rock being modified
	 */
	private class RockBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Only work the notification if a rock is selected
			if(RockMenu.this.rock == null) {
				return;
			}
			
			int rockId = intent.getExtras().getInt("id", Rock.BLANK_ROCK_ID);
			
			if(RockMenu.this.rock.getId() == rockId) {
				if(intent.getAction() == Rock.ACTION_UPDATED) {
					handleUpdatedRock(context, intent.getExtras().getInt("id"));
					
				} else if (intent.getAction() == Rock.ACTION_DELETED) {
					handleDeletedRock(context, intent.getExtras().getInt("id"));
				}
			}
		}
	
		private void handleUpdatedRock(Context context, int rockId) {
			// Replace model rock
			RockMenu.this.rock = Rock.getRock(context, rockId);
			
			if(RockMenu.this.rock != null) {
				RockMenu.this.rock.setComments(comments.getText().toString());
				// And update the menu
				updateMenu();
			}	
		}
		
		private void handleDeletedRock(Context context, int rockId) {
			// Just in case the delete logic doesn't change states
			hide();
		}
	}
	
	private class RockImageActionModeCallback implements ActionMode.Callback {
		// Call when startActionMode() is called
		// Should inflate the menu
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.rock_image, menu);
			return true;
		}
		
		// Called when the mode is invalidated
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			boolean result = false;
			if(RockMenu.this.rock == null){
				Log.d("RockMenu", "Null rock");
			}else if(RockMenu.this.rock.getPicture() == null) {
				menu.removeItem(R.id.rock_image_delete);
				result = true;
			}
			return result;
		}
		
		// Called when the user selects a menu item
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			boolean result;
			
			switch(item.getItemId()) {
				case R.id.rock_image_delete:
					showConfirmRockImageDeleteAlert(mode);
					result = true;
				break;
				
				case R.id.rock_image_camera:
					// Take a picture because one does not exist
					Intent intent = new Intent(RockMenu.ACTION_TAKE_PICTURE);
					intent.putExtra("id", RockMenu.this.rock.getId());
					intent.putExtra("path", Rock.IMAGE_PATH);
					intent.putExtra("filename", String.format(Rock.IMAGE_FILENAME_PATTERN, rock.getId()));
				
					// Broadcast intent (We have to ask the activity to take the picture so it comes back as a result)
					LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
					
					result = true;
					// No longer need to show the action bar after taking a new picture
					mode.finish();
				break;
				
				default:
					result = false;
				break;
			}
			
			return result;
		}

		// Called when the user exists the action mode
		public void onDestroyActionMode(ActionMode mode) {
			imageActionMode = null;
		}
		
		/* Creates a dialog which the user can use to confirm a rock image delete
		 */
		private void showConfirmRockImageDeleteAlert(final ActionMode mode) {
			
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			
			builder.setTitle(R.string.rock_image_delete);
			builder.setMessage(R.string.rock_image_delete_title);
			
			builder.setPositiveButton(R.string.rock_image_delete_yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					RockMenu.this.rock.deletePicture();
					
					// Update the action to remove the delete option
					mode.invalidate();
				}
			});
			
			builder.setNegativeButton(R.string.rock_image_delete_no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// Do nothing... The app is much less interesting...
				}
			});
			
			builder.create().show();
		}
	}

	@Override
	public void onClick(View arg0) {
		if(arg0.getId() == R.id.button_picture){
			if(RockMenu.this.rock == null){
				Log.d("RockMenu", "Rock null");
			} else {
				if(RockMenu.this.rock.getPicture() == null) {
					// Take a picture because one does not exist
					Intent intent = new Intent(RockMenu.ACTION_TAKE_PICTURE);
					intent.putExtra("id", RockMenu.this.rock.getId());
					intent.putExtra("path", Rock.IMAGE_PATH);
					intent.putExtra("filename", String.format(Rock.IMAGE_FILENAME_PATTERN, rock.getId()));
				
					// Broadcast intent (We have to ask the activity to take the picture so it comes back as a result)
					LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
				} else {
					// start an image viewing activity with image file 
					Intent intent = new Intent();  
					intent.setAction(android.content.Intent.ACTION_VIEW);  
					
					File file = new File(Rock.IMAGE_PATH, String.format(Rock.IMAGE_FILENAME_PATTERN, rock.getId()));  
					intent.setDataAndType(Uri.fromFile(file), "image/png");  
					context.startActivity(intent);
				}
			}
		} else if(arg0.getId() == R.id.button_picked){
			Log.d("RockMenu", "Toggle Picked");
			// Toggle the current rock
			Rock rock = RockMenu.this.rock;
			if(rock != null){
				rock.setPicked(!rock.isPicked());
				rock.setChanged(true);
				rock.save();
				markerHandler.changeMarkerIcon(rock);
				this.updateMenu();
			} else {
				//Error
				Log.d("RockMenu - onClick", "Error - null rock when trying to pickup");
			}
		}
	}
	
}
