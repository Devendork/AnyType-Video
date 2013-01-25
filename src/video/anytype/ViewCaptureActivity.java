package video.anytype;

/***
 * This class shows the captured shape just after it is photographed
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import video.anytype.R.id;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.view.ViewGroup.LayoutParams;

//each activity is a state. 
//this is the photo capture activity. It takes a picture 
public class ViewCaptureActivity extends Activity implements OnTouchListener{
	

	protected static final String TAG = "ViewCaptureActivity";
	private DrawShapeOnTop shapeView;
	private boolean two_finger;
	private boolean started_saving = false;
	private double beginTime = System.currentTimeMillis();


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Globals.updateActiveThreads();
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.videoviewcapture);

		Log.d("View Activity", "View Activity Loaded");
		
		Button playVideo = (Button) findViewById(id.button_playback);
		//first - see if there is a video attached to this stage
		if(Globals.stageHasVideo()){
			playVideo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//launch the video playback intent
				writeToLog("_VideoPlayer");
				openVideoPlayer();
			}
		});
		}else{
			playVideo.setVisibility(View.INVISIBLE);
		}
		
		
		
		// add accept button listener
		Button acceptButton = (Button) findViewById(id.button_accept);
		acceptButton.setBackgroundColor(Color.rgb(255, 33, 177));
		acceptButton.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Log.d("Tap", "Event Source "+event.getSource());
				
				v.setBackgroundColor(Color.CYAN);
				if(!started_saving) return false;
				else return true;
				
			}
		});
		
		acceptButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				v.setEnabled(false);
				Thread t =  new Thread(new Runnable() {
				    	int stage = Globals.stage;
				    	
				    	@Override
				        public void run() {
				    	Log.d("Thead", "Running Thread "+stage);
				          saveImages(stage, shapeView.getShapeImageOut());      //this needs to execute before 
						  Globals.buildLetters(stage); //this is called
				        }
				    });
				Globals.addNewThread(t);
		
				
			    Globals.nextStage();
			    setResult(1);
			    
			    if(Globals.edit) nextEditScreen();
			    else finish();
			}
		});

		// add accept button listener
		Button rejectButton = (Button) findViewById(id.button_reject);
		//if(Globals.edit) rejectButton.setVisibility(View.INVISIBLE);
		rejectButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// get an image from the camera
				setResult(0);  //return false to retake
				writeToLog("_reject");	
				if(Globals.edit) retakeScreen();
			    else finish();
			}
		});
		

		// Create our Preview view and set it as the content of our activity.
		shapeView = new DrawShapeOnTop(this, Globals.getStageShape(), true);
		shapeView.setOnTouchListener(this);
		FrameLayout preview = (FrameLayout) findViewById(id.camera_preview);
		preview.addView(shapeView, new LayoutParams(Globals.preview_size.x,Globals.preview_size.y));
		

	}
	
	public void retakeScreen(){
		Intent intent = new Intent(this, VideoCaptureActivity.class);
		startActivity(intent);
	}
	
	public void nextEditScreen(){
		Intent intent;
		if(Globals.stage <= 4){
			File vfile = new File(Globals.getStageVideoPath(Globals.stage));
			File pfile = new File(Globals.getStagePhotoPath(Globals.stage));
			
			
			if(!vfile.exists() || !pfile.exists()) intent = new Intent(this, VideoCaptureActivity.class);
			else  intent = new Intent(this, ViewCaptureActivity.class);
			startActivity(intent);
			
		}else{
		    intent = new Intent(this, CanvasActivity.class);
			startActivity(intent);
		}
	}
	
	
	public void writeToLog(String to){
		double endTime = System.currentTimeMillis();
		double time = endTime - beginTime;
		Globals.writeToLog(this, getLocalClassName(), to, time);
	}

	
	public void openVideoPlayer(){
		Intent intent = new Intent(this, VideoPlayerActivity.class);
		startActivity(intent);
	}
	
	// Implement the OnTouchListener callback
	public boolean onTouch(View v, MotionEvent event) {
		Log.d("Touch", "Action: " + event.getAction());
		Log.d("Touch", "Action Index: " + event.getActionIndex());

		DrawShapeOnTop dv = (DrawShapeOnTop) v;
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			dv.startPath(event.getX(), event.getY());
			writeToLog("_DrawPath");

			// finger up - nothing selected
		} else if (event.getAction() == MotionEvent.ACTION_UP) {			
			dv.endPath(event.getX(), event.getY());

		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			dv.addPathPoint(event.getX(), event.getY());
		}
		
		
		if(dv.isValidPath()) dv.invalidate();
		return true;

	}
	
	public void saveImages(int stage, Bitmap bmap){
		Log.d("Thead", "Enter Save Images "+stage+" && bitmap "+bmap);

		
		File pictureFile = Globals.getOutputMediaFile(Globals.MEDIA_TYPE_IMAGE, "IMG_" + Integer.toString(stage) + "_CROP.png");
		if (pictureFile == null) return;


		try {
			FileOutputStream fos = new FileOutputStream(pictureFile);
			bmap.compress(Bitmap.CompressFormat.PNG, 60, fos);
			fos.close();
			Log.d("Capture Activity", "File Created");
			

		} catch (FileNotFoundException e) {
			Log.d("Thead", "File not found: " + e.getMessage());
		} catch (IOException e) {
			Log.d("Thead", "Error accessing file: " + e.getMessage());
		}
		
		if(Globals.stageHasVideo(stage)){
			try{
				Globals.getShape(stage).makeVideoFrames(Globals.getStageVideoPath(stage));
			}catch(Exception e){
				Log.d("Thread", "Error making video stuff");
			}
		}
		
		
		Log.d("Thead", "File Exists "+pictureFile.exists()+ "Path:"+pictureFile.getPath());
		Log.d("Thead", "Exit Save Images ");

	}
	


	

}





