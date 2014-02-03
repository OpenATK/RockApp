package com.openatk.rockapp;

import java.io.File;
import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.maps.model.LatLng;
import com.openatk.rockapp.R;
import com.openatk.rockapp.db.DatabaseHelper;
import com.openatk.rockapp.libcommon.ImageTools;
import com.openatk.rockapp.libcommon.SlideLayout;
import com.openatk.rockapp.models.Rock;

public class RockMenu extends SlideLayout implements OnClickListener {
	private EditText comments;
	private ImageButton picked;
	private ImageButton picture;
	private Drawable pictureDrawable;
	private Rock rock = null;
	private Context context;
	private MarkerHandler markerHandler;
	
	private DatabaseHelper dbHelper = null;
	private RockMenuListener listener = null;
	
	private LatLng newLatLng = null;

	private ActionMode editActionMode;
	private ActionMode imageActionMode;
	
	public interface RockMenuListener {
		public void	RockTakePicture(int id, String path, String filename);
	}
	
	public RockMenu(final Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		// Start off not editing a rock
		rock = null;
	}
	
	public void setListener(RockMenuListener listener){
		this.listener = listener;
	}
	
	public void setDBHelper(DatabaseHelper dbHelper){
		this.dbHelper = dbHelper;
	}
	
	public void setMarkerHandler(MarkerHandler theMarkerHandler) {
		markerHandler = theMarkerHandler;
	}

	/*
	 * Have to wait for children to inflate before getting pointers to them
	 */
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		// Get a hold of the view
		comments = (EditText) findViewById(R.id.comments);
		picked = (ImageButton) findViewById(R.id.button_picked);
		picture = (ImageButton) findViewById(R.id.button_picture);
		pictureDrawable = context.getResources().getDrawable(R.drawable.camera);

		// Listen for changes to the text box so they can be saved to the rock
		comments.addTextChangedListener(new CommentTextChangeListener());

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
		if (editActionMode != null) {
			editActionMode.finish();
		}

		// Remove action mode from image if it exists
		if (imageActionMode != null) {
			imageActionMode.finish();
		}
	}

	/*
	 * Transitions the edit view to a new model rock
	 */
	public void editRock(Rock rock) {
		this.rock = rock;
		// Update the view to the new model
		updateMenu();
	}

	public void setNewLatLng(LatLng position) {
		newLatLng = position;
		// Set rock so if save it will save this too
		this.rock.setLon(newLatLng.longitude);
		this.rock.setLat(newLatLng.latitude);
		
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		this.rock.save(database);
		database.close();
		dbHelper.close();
	}

	/*
	 * Update the view from the current rock model
	 */
	private void updateMenu() {
		if (rock == null) {
			return;
		}

		comments.setText(rock.getComments());

		if (rock.isPicked()) {
			picked.setSelected(true);
		} else {
			picked.setSelected(false);
		}

		String imagePath = rock.getPicture();
		if (imagePath != null) {
			picture.setImageBitmap(ImageTools.sizePicture(imagePath, pictureDrawable.getIntrinsicHeight(), pictureDrawable.getIntrinsicWidth()));
		} else {
			picture.setImageDrawable(pictureDrawable);
		}
	}

	public void delete() {
		this.rock.setDeleted(true);
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		this.rock.save(database);
		database.close();
		dbHelper.close();
		this.rock = null;
	}


	/*
	 * Focus Change Listener which watches for possible changes in the rocks
	 * comments
	 */
	private class CommentTextChangeListener implements TextWatcher {
		 public void afterTextChanged(Editable s) {
            if (RockMenu.this.rock != null) {
				Log.d("CommentTextChangeListener", "Saving comment");
				RockMenu.this.rock.setComments(comments.getText().toString());
				RockMenu.this.rock.setHasSynced(false);
				RockMenu.this.rock.setCommentsChanged(new Date()); //TODO internet time
				
				SQLiteDatabase database = dbHelper.getWritableDatabase();
				RockMenu.this.rock.save(database);
				database.close();
				dbHelper.close();
				//if(trelloController != null) trelloController.syncDelayed();
			} else {
				Log.d("CommentTextChangeListener", "Rock is null");
			}
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
        public void onTextChanged(CharSequence s, int start, int before, int count){}
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
			if (RockMenu.this.rock == null) {
				Log.d("RockMenu", "Null rock");
			} else if (RockMenu.this.rock.getPicture() == null) {
				menu.removeItem(R.id.rock_image_delete);
				result = true;
			}
			return result;
		}

		// Called when the user selects a menu item
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			boolean result;

			switch (item.getItemId()) {
			case R.id.rock_image_delete:
				showConfirmRockImageDeleteAlert(mode);
				result = true;
				break;

			case R.id.rock_image_camera:
				// Take a picture because one does not exist
				/*Intent intent = new Intent(RockMenu.ACTION_TAKE_PICTURE);
				intent.putExtra("id", RockMenu.this.rock.getId());
				intent.putExtra("path", Rock.IMAGE_PATH);
				intent.putExtra("filename", String.format(Rock.IMAGE_FILENAME_PATTERN, rock.getId()));

				// Broadcast intent (We have to ask the activity to take the
				// picture so it comes back as a result)
				LocalBroadcastManager.getInstance(context).sendBroadcast(intent);*/
				listener.RockTakePicture(RockMenu.this.rock.getId(), Rock.IMAGE_PATH, String.format(Rock.IMAGE_FILENAME_PATTERN, RockMenu.this.rock.getId()));
				result = true;
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

		/*
		 * Creates a dialog which the user can use to confirm a rock image
		 * delete
		 */
		private void showConfirmRockImageDeleteAlert(final ActionMode mode) {

			AlertDialog.Builder builder = new AlertDialog.Builder(context);

			builder.setTitle(R.string.rock_image_delete);
			builder.setMessage(R.string.rock_image_delete_title);

			builder.setPositiveButton(R.string.rock_image_delete_yes,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							RockMenu.this.rock.deletePicture();
							SQLiteDatabase database = dbHelper.getWritableDatabase();
							RockMenu.this.rock.save(database);
							database.close();
							dbHelper.close();
							// Update the action to remove the delete option
							mode.invalidate();
						}
					});

			builder.setNegativeButton(R.string.rock_image_delete_no,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// Do nothing... The app is much less interesting...
						}
					});

			builder.create().show();
		}
	}

	@Override
	public void onClick(View arg0) {
		if (arg0.getId() == R.id.button_picture) {
			if (RockMenu.this.rock == null) {
				Log.d("RockMenu", "Rock null");
			} else {
				if (RockMenu.this.rock.getPicture() == null) {
					// Take a picture because one does not exist
					listener.RockTakePicture(RockMenu.this.rock.getId(), Rock.IMAGE_PATH, String.format(Rock.IMAGE_FILENAME_PATTERN, RockMenu.this.rock.getId()));
				} else {
					// start an image viewing activity with image file
					Intent intent = new Intent();
					intent.setAction(android.content.Intent.ACTION_VIEW);
					File file = new File(Rock.IMAGE_PATH, String.format(Rock.IMAGE_FILENAME_PATTERN, rock.getId()));
					intent.setDataAndType(Uri.fromFile(file), "image/png");
					context.startActivity(intent);
				}
			}
		} else if (arg0.getId() == R.id.button_picked) {
			Log.d("RockMenu", "Toggle Picked");
			// Toggle the current rock
			Rock rock = RockMenu.this.rock;
			if (rock != null) {
				rock.setPicked(!rock.isPicked());
				rock.setHasSynced(false);
				rock.setPickedChanged(new Date()); //TODO internet time
				SQLiteDatabase database = dbHelper.getWritableDatabase();
				rock.save(database);
				database.close();
				dbHelper.close();
				
				markerHandler.changeMarkerIcon(rock);
				this.updateMenu();
			} else {
				// Error
				Log.d("RockMenu - onClick", "Error - null rock when trying to pickup");
			}
		}
	}

}
