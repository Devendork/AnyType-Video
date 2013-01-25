package firstsubtext.subtext;

/***
 * This class draws a shape to the screen and allows the user 
 * to capture that shape
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import data.Shape;
import firstsubtext.subtext.R.id;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnGenericMotionListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
//each activity is a state. 
//this is the photo capture activity. It takes a picture 


public class CanvasActivity extends Activity implements OnTouchListener {

	private GridView letter_grid;
	private LetterView letter_view;
	private boolean two_finger = false;
	private int savedNum = 0;
	private boolean touch_play = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("Canvas Call", "Entered ON Create");
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		Log.d("Canvas Call", "Set Content View");
		setContentView(R.layout.canvas);
		
		
		Log.d("Canvas Call", "Find Grid View");
		letter_grid = (GridView) findViewById(R.id.letter_grid);
		letter_grid.setAdapter(new LetterAdapter(this));

		
		
//		letter_grid.setOnItemClickListener(new OnItemClickListener() {
//			public void onItemClick(AdapterView<?> parent, View v,
//					int position, long id) {
//				Log.d("Delete", "Item "+position+"selected");
//
//				addToCanvas(position);
//			}
//		});
//		
//		letter_grid.setOnItemLongClickListener(new OnItemLongClickListener(){
//			public boolean onItemLongClick(AdapterView<?> parent, View v,
//					int position, long id) {
//				addToCanvas(position);
//				return true;
//			}
//		});
//		
//						
//		Log.d("Delete", "Something");
//
//		letter_grid.setOnItemSelectedListener(new OnItemSelectedListener(){
//
//			@Override
//			public void onItemSelected(AdapterView<?> parent, View v,
//					int position, long id) {
//				Log.d("Delete", "Item "+position+"selected");
//				
//			}
//
//			@Override
//			public void onNothingSelected(AdapterView<?> parent) {
//				Log.d("Delete", "Nothing selected");
//				
//			}});
//		
		
		
		
		
	
		letter_view = new LetterView(this);
		FrameLayout canvas = (FrameLayout) findViewById(R.id.canvas_frame);
		canvas.addView(letter_view, new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		letter_view.setOnTouchListener(this);
		
		
		if(Globals.saved_lv != null){
			letter_view.loadState(Globals.saved_lv);
			Globals.saved_lv = null;
		}
		
		Log.d("Canvas Call", "Ended Canvas");

		Button saveButton = (Button) findViewById(id.button_save_canvas);
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveScreen();
			}
		});
		
		Button resetButton = (Button) findViewById(id.button_reset);
		resetButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				reset();
			}
		});
		
		Button editButton = (Button) findViewById(id.button_edit);
		editButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				launchEditor();
			}
		});
		
		
		Button saveFontButton = (Button) findViewById(id.button_save_font);
		saveFontButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
	            //Toast.makeText("Something", Toast.LENGTH_SHORT).show();
				saveFontDialog();
			}
		});
		
		//set a mode for deleteing
		Switch playMode = (Switch) findViewById(id.switch_delete_mode);
		playMode.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				updateTouchMode();
			}
			

		});
		
		
		if(Globals.hasAnyVideos()){
			playMode.setVisibility(View.VISIBLE);
			touch_play = playMode.isChecked();
		}else{
			touch_play = false;
			playMode.setChecked(false);
			playMode.setVisibility(View.INVISIBLE);
		}
		
	
	
		
	}
	
	public void saveFontDialog(){
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Save As");
		alert.setMessage("Enter a Name for this Typeface");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		alert.setView(input);

		
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		  String value = input.getText().toString();
		  	boolean success = Globals.renameDirectory(value);
		  	Log.d("Delete", "Rename: "+success);
		  }
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});

		alert.show();
	}
	
	
	private void launchEditor(){
		Intent intent = new Intent(this, EditActivity.class);
		startActivity(intent);
	}
	
	@Override
	protected void onPause() {
		Log.d("Delete Mode", "On Pause Called");
		saveCanvasState();
		super.onPause();
	}

	void saveCanvasState(){
		Globals.saved_lv = letter_view;
	}
	
	
	private void updateTouchMode(){
		touch_play = !touch_play;
		Log.d("Delete Mode", "New Value"+touch_play);

	}
	
	private void saveScreen(){
		//save the picture

		File pictureFile = Globals.getOutputMediaFile(Globals.MEDIA_TYPE_IMAGE, "MyCanvas_" +(savedNum++)+ Globals.timeStamp + ".png");
		
		if (pictureFile == null) {
			return;
		}


		try {
			FileOutputStream fos = new FileOutputStream(pictureFile);
			Bitmap bmap = letter_view.getImageOut();
			bmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
			fos.close();
			Log.d("Capture Activity", "File Created");
			

		} catch (FileNotFoundException e) {
			Log.d("", "File not found: " + e.getMessage());
		} catch (IOException e) {
			Log.d("", "Error accessing file: " + e.getMessage());
		}
		
	}
	
	public void reset(){
		Intent intent = new Intent(this, LoadActivity.class);
		startActivity(intent);
	}
	


	public void addToCanvas(ImageView v, MotionEvent e) {
		float x = (letter_view.getWidth()/26*v.getId())+e.getX();
		
		
		letter_view.addLetter(v.getId());
		letter_view.invalidate();
		letter_view.updatePosition(x, 61f);
		
		Log.d("Delete", "POS: " + e.getX());
		
    	


		MotionEvent motionEvent = MotionEvent.obtain(e);
		e.setAction(MotionEvent.ACTION_DOWN);
		e.setLocation(x, 65f);
		motionEvent = MotionEvent.obtainNoHistory(e);
		letter_view.dispatchTouchEvent(motionEvent);
		
//		MotionEvent copy = MotionEvent.obtain(e);
//    	copy.setAction(MotionEvent.ACTION_UP);
//    	boolean success = v.dispatchTouchEvent(copy);
//		Log.d("Delete", "Cancel Success?: " + success);
		
//		
//		MotionEvent motionEvent = MotionEvent.obtain(
//			    10, 
//			    10, 
//			    MotionEvent.ACTION_DOWN, 
//			    x, 
//			    65f, 
//			    metaState
//			);
//
//			// Dispatch touch event to view
		
		/*

		e.setLocation(x, 65f);

		
		e.setAction(MotionEvent.ACTION_UP);
		onTouch(letter_view, e);
		
		e.setAction(MotionEvent.ACTION_DOWN);
		//e.setLocation(x, 65f);
		onTouch(letter_view, e);
		
		e.setAction(MotionEvent.ACTION_MOVE);
		//e.setLocation(x, 65f);
		onTouch(letter_view, e);
		*/

		//pass the touch event off to the new handler
	}

	// Implement the OnTouchListener callback
	public boolean onTouch(View v, MotionEvent event) {
		Log.d("Delete", "Action: " + event.getAction());
		Log.d("Delete", "Action Index: " + event.getActionIndex());

		int selected;
		LetterView lv = (LetterView) v;


		if (event.getActionIndex() > 0) {
			two_finger = !two_finger;
			
			if(event.getActionIndex() > 1){
				lv.removeCurrentLetter();
				
			}
			
		} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
			
		
			selected = lv.locate((int) event.getX(), (int) event.getY());
			Log.d("Delete", "Selected Letter "+selected+" Cur "+lv.getCur());

			
			if (selected != lv.getCur() && lv.getCur() != -1)
				lv.deselect(lv.getCur());
			if (selected == -1)
				return true;
			lv.select(selected);
			
			if(touch_play){
				//lv.removeCurrentLetter();
				if(lv.getCur() != -1){
				Log.d("Delete", "Setting Force Letter to "+lv.getCur());
				Globals.force_letter = lv.getCurLetterId();
				
				saveCanvasState();
				
				Intent intent = new Intent(this, LetterVideoPlayerActivity.class);
				startActivity(intent);
				}
			}


			// finger up - nothing selected
		} else if (event.getAction() == MotionEvent.ACTION_UP) {			
			
			selected = lv.getCur();
			if (selected != -1)
				lv.deselect(selected);

		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			selected = lv.getCur();
			if (selected == -1)
				return true;
			
			lv.updatePosition(event.getX(), event.getY());

			Log.d("Delete", "Update Position");
			if (two_finger) {
				try {

					//make sure it does one or the other but not both
					lv.updateScale(Globals.getScale(event.getX(0),
							event.getY(0), event.getX(1), event.getY(1)));
					
					
//					lv.setRotations(Globals.getRotation(event.getX(0),
//							event.getY(0), event.getX(1), event.getY(1)));
					
					float[] center = Globals.getCenter(event.getX(0),
							event.getY(0), event.getX(1), event.getY(1));
					lv.updatePosition(center[0], center[1]);

				} catch (IllegalArgumentException e) {
					Log.d("Matrix", "Exception Scale: " + e.getMessage());
					two_finger = false;
				}


			}

		}
		lv.invalidate();
		return true;

	}

}
