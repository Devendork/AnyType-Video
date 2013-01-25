package video.anytype;


/**
 * This class starts all of the global variables.  It launches the application and moves on
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
import java.util.HashMap;
import java.util.Iterator;

import video.anytype.R.id;

import data.Letter;
import data.Shape;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.view.ViewGroup.LayoutParams;

//each activity is a state. 
//this is the photo capture activity. It takes a picture 
public class LoadFontActivity extends Activity {

	
	protected static final String TAG = null;
	private String filepath;
	private HashMap<Integer, String> files = new HashMap();
	private LinearLayout list;
	private double beginTime = System.currentTimeMillis();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.load);

	    list = (LinearLayout) findViewById(R.id.list);
		File dir = new File(Globals.getBasePath());
        String[] children = dir.list();

        
        if (children == null) {
            // Either dir does not exist or is not a directory
        } else {
            for (int i=0; i<children.length; i++) {
                String filename = children[i];
            	if(Globals.directoryHasAnyVideos(filename)){
            	Button b = new Button(this);
                files.put(i, filename);
                b.setId(i);
                b.setText(filename);
                b.setWidth(400);
                b.setGravity(Gravity.CENTER);
                b.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View view) {
						Log.d("Tap", "Clicked "+files.get(view.getId()));
						if(Globals.edit) launchEdit(files.get(view.getId()));
						else openFont(files.get(view.getId()));
					}
                	
                });
                
                b.setOnTouchListener(new OnTouchListener(){
                
                	
					@Override
					public boolean onTouch(View view, MotionEvent arg1) {
						Button but = (Button) view;
						//filepath = but.getText().toString();
						
						but.setText("Loading ...");
						Log.d("Tap", "Touched");
						return false;
					}
                	
                });
                
                
                list.addView(b, new LayoutParams(LayoutParams.WRAP_CONTENT,
        				LayoutParams.WRAP_CONTENT));
            	}
            }
        }
		

		
	
	}
	
	public void launchEdit(String s){
		Globals.base_dir_name = s;
		Globals.edit = true;

		
		double endTime = System.currentTimeMillis();
		double time = endTime - beginTime;
		Globals.writeToLog(this, super.getLocalClassName(), "ViewCapture - "+s, time);
		
		Intent intent = new Intent(this, ViewCaptureActivity.class);
		startActivity(intent);
	}
	
	
	public void openFont(String s){
		Globals.base_dir_name = s;
		
		
		for(int i = 0; i < Globals.shapes.length; i++){
			Log.d("Frames", "Loading Frames for "+i);
			Globals.buildLetters(i);
			Globals.getShape(i).loadVideoFrames();
		}
	
		
		double endTime = System.currentTimeMillis();
		double time = endTime - beginTime;
		Globals.writeToLog(this, super.getLocalClassName(), "CanvasActivity - "+s, time);
		
		Intent intent = new Intent(this, CanvasActivity.class);
		startActivity(intent);
	}
	
	
	
	@Override
	public void onRestart() {
		
		//change all the names back to what they are
		for(int i = 0; i < list.getChildCount(); i++){
			Button b = (Button) list.getChildAt(i);
			b.setText(files.get(b.getId()));
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
	}



}
