package firstsubtext.subtext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import firstsubtext.subtext.R.id;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class VideoCaptureActivity extends Activity {
	private Camera mCamera;
	private SurfaceView mPreview;
	private MediaRecorder mMediaRecorder;
	private static DrawShapeOnTop shapeView;
	private FrameLayout preview;
	private boolean isRecording = false;
	private Timer timer;
	private int max_time = 15000;
	private TimerTask timer_task;
	private int interval = 1;
	private ProgressBar mProgress;
	private int ticks = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.videocameracapture);

		Log.d("Video Canvas Call", "On Create Called");
	    mProgress  = (ProgressBar) findViewById(id.progress_bar);
		mProgress.setMax(max_time);
		mProgress.setProgress(ticks);
		mProgress.setVisibility(View.INVISIBLE);
		
		// Spawn a thread to build the letters
		// Start lengthy operation in a background thread
		timer_task = new TimerTask() {
			Handler handler = new Handler();
			public void run() {
				handler.post(new Runnable(){
					public void run(){
						mProgress.setProgress(++ticks);
						if(ticks == max_time) endRecording();
					}
				});				
			}
		};


		// if we've captured all the images, move to the canvas stage
		if (Globals.stage > 4) {
			Globals.buildLetters();
			Intent intent = new Intent(this, CanvasActivity.class);
			startActivity(intent);
			Globals.resetStage();

		} else {
			

			Thread buildLettersThread = new Thread(new Runnable() {
				@Override
				public void run() {
					Globals.buildLetters();
				}
			});
			buildLettersThread.setDaemon(true);
			buildLettersThread.start();

			Button captureButton = (Button) findViewById(id.button_capture);
			captureButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (isRecording) {
						timer.cancel();
						endRecording();
					} else {
						mCamera.takePicture(null, null, mPicture);
					}
				}
			});

			// Create an instance of Camera
			mCamera = getCameraInstance();
			mPreview = new CameraPreview(this, mCamera);
			// prepareVideoRecorder();

			// Create our Preview view and set it as the content of our
			// activity.
			shapeView = new DrawShapeOnTop(this, Globals.getStageShape(), false);

			preview = (FrameLayout) findViewById(id.camera_preview);
			preview.addView(mPreview);
			preview.addView(shapeView, new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			
			
			SeekBar seek = (SeekBar) findViewById(id.seek);
			seek.setProgress(0);
			Camera.Parameters cp = mCamera.getParameters();
			if(cp.isZoomSupported()){
				seek.setVisibility(View.VISIBLE);
				seek.setMax(cp.getMaxZoom());
				seek.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
		
					@Override
					public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
						// TODO Auto-generated method stub
						Camera.Parameters cp = mCamera.getParameters();
						cp.setZoom(arg1);
						mCamera.setParameters(cp);
					}
		
					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub
						
					}
		
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub
						
					}
					
				});
			}else{
				seek.setVisibility(View.INVISIBLE);
			}


		}

	}


	
	private void startTimer() {
		mProgress.setProgress(ticks);
		mProgress.setVisibility(View.VISIBLE);
		
		timer = new Timer();
		timer.scheduleAtFixedRate(timer_task, 0, interval);
	}
	




	private void endRecording() {
		// stop recording and release camera
		mMediaRecorder.stop(); // stop the recording
		releaseMediaRecorder(); // release the MediaRecorder object
		mCamera.lock(); // take camera access back from MediaRecorder

		// inform the user that recording has stopped
		setCaptureButtonText("Capture");
		isRecording = false;

		nextScreen();
	}

	private void initRecording() {
		Log.d("Video", "Started Recording");

		// initialize video camera
		if (prepareVideoRecorder()) {
			Log.d("Video", "Start Recording");

			// Camera is available and unlocked, MediaRecorder is prepared,
			// now you can start recording
			mMediaRecorder.start();

			// inform the user that recording has started
			setCaptureButtonText("Stop");
			isRecording = true;

			startTimer();
		} else {
			// prepare didn't work, release the camera
			releaseMediaRecorder();
			// inform user
		}
	}

	private void setCaptureButtonText(String text) {
		Button captureButton = (Button) findViewById(id.button_capture);
		captureButton.setText(text);

	}

	private void nextScreen() {
		Log.d("Capture Activity", "Create Intent");
		Intent intent = new Intent(this, ViewCaptureActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onPause() {
		super.onPause();
		releaseMediaRecorder(); // if you are using MediaRecorder, release it
		releaseCamera(); // release the camera immediately on pause event
	}

	private void releaseMediaRecorder() {
		if (mMediaRecorder != null) {
			mMediaRecorder.reset(); // clear recorder configuration
			mMediaRecorder.release(); // release the recorder object
			mMediaRecorder = null;
			mCamera.lock(); // lock camera for later use
		}
	}

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.release(); // release the camera for other applications
			mCamera = null;
		}
	}

	private boolean prepareVideoRecorder() {

		// mCamera = getCameraInstance(); //already called before this function
		mMediaRecorder = new MediaRecorder();

		// Step 1: Unlock and set camera to MediaRecorder
		mCamera.unlock();
		mMediaRecorder.setCamera(mCamera);

		// Step 2: Set sources
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

		// Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
		mMediaRecorder.setProfile(CamcorderProfile
				.get(CamcorderProfile.QUALITY_HIGH));

		// Step 4: Set output file
		mMediaRecorder.setOutputFile(getOutputMediaFile(
				Globals.MEDIA_TYPE_VIDEO).toString());

		// Step 5: Set the preview output
		mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

		// Step 6: Prepare configured MediaRecorder
		try {
			mMediaRecorder.prepare();
		} catch (IllegalStateException e) {
			Log.d("Video Capture",
					"IllegalStateException preparing MediaRecorder: "
							+ e.getMessage());
			releaseMediaRecorder();
			return false;
		} catch (IOException e) {
			Log.d("Video Capture",
					"IOException preparing MediaRecorder: " + e.getMessage());
			releaseMediaRecorder();
			return false;
		}
		return true;
	}

	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
			System.out.println("camera does not exist or is in use");
		}
		return c; // returns null if camera is unavailable
	}

	private static File getOutputMediaFile(int type) {
		File mediaFile;
		if (type == Globals.MEDIA_TYPE_IMAGE) {
			mediaFile = new File(Globals.getTestPath() + File.separator
					+ "IMG_" + Integer.toString(Globals.stage) + ".jpg");
		} else if (type == Globals.MEDIA_TYPE_VIDEO) {
			mediaFile = new File(Globals.getTestPath() + File.separator
					+ "VID_" + Integer.toString(Globals.stage) + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}

	// this is called when the data is ready
	private PictureCallback mPicture = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.d("Video Activity", "Picture Taken");

			File pictureFile = getOutputMediaFile(Globals.MEDIA_TYPE_IMAGE);
			if (pictureFile == null) {
				return;
			}

			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
				Log.d("Capture Activity", "File Created");

			} catch (FileNotFoundException e) {
				Log.d("", "File not found: " + e.getMessage());
			} catch (IOException e) {
				Log.d("", "Error accessing file: " + e.getMessage());
			}

			initRecording();

			// let's spawn a background thread to do the letters
			// Globals.build_thread.execute();

		}

	};

}
