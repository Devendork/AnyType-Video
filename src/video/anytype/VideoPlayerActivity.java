package firstsubtext.subtext;


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
import java.util.Iterator;

import data.Letter;
import data.Shape;
import firstsubtext.subtext.R.id;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.VideoView;
import android.view.ViewGroup.LayoutParams;

//each activity is a state. 
//this is the photo capture activity. It takes a picture 
public class VideoPlayerActivity extends Activity {

	protected static final String TAG = null;
	private static int stage = 0; // keeps a record of which shape we're
	private VideoView	mVideo;					


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.videoplayer);

		mVideo = (VideoView) findViewById(R.id.video_viewer);
	    MediaController controller = new MediaController(this);
	    mVideo.setMediaController(controller);
	    mVideo.setVideoPath(Globals.getStageVideoPath());
	    mVideo.start();
	    
	    //force this window to close when the video is through playing
	    mVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
            	finish();
            }
        });
	    
	   

	}

	@Override
	public void onRestart() {

	}

	@Override
	protected void onPause() {
		super.onPause();
	}



}
