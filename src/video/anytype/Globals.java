package video.anytype;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import data.*;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.View;

public class Globals {
	static String timeStamp;
	static String startDateTime;
	static File mediaStorageDir;

	static Shape[] shapes;
	static Letter[] letters;
	static boolean[] existing_letters;
	static int stage;
	public static float shapeStretch = 2.0f;
	public static float shapeShrink = 0.5f; //used to make video bitmaps smaller
	public static int letter_size = 600;
	static int grab_num = 0;
	static String base_dir_name;
	static int background_alpha = 150;
	static int line_num = 0;
	static String save_string = "";
	static boolean edit = false;
	static int buffer_size = 3;

	//static BuildLettersTask build_thread = new BuildLettersTask();

	
	static boolean playback_mode = false;
	static int force_stage = 0;
	static int force_letter = 0;
	
	static Point screen_size;
	static Point picture_size = new Point();
	public static Point preview_size = new Point();
	static Point video_preview_size = new Point();
	static Point video_size = new Point();
	static double aspect_ratio;
	
	static double longitude;
	static double latitude;
	static LocationManager lm;
	
	static double sample_rate = 0.5; //every half second, show a frame
	

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	private static float[] lastfinger1 = new float[2];
	private static float[] lastfinger2 = new float[2];
	
	static LetterView saved_lv = null;
	
	static Bitmap[] rawImages; 
	static LinkedList threads = new LinkedList();
	

	
	
	
	
	
	public Globals(Point screen, Context context) {
		screen_size = new Point(screen);
		
		lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE); 

		startDateTime = new SimpleDateFormat("yyyy : MM : dd : HH : mm : ss").format(new Date());

		
	    LocationListener locationListener = new LocationListener() {
		    public void onLocationChanged(Location location) {
		        longitude = location.getLongitude();
		        latitude = location.getLatitude();
		    }

			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// TODO Auto-generated method stub
				
			}
		};
		
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);

		
		rawImages = new Bitmap[5];
		
		existing_letters = new boolean[26];
		for (int i = 0; i < existing_letters.length; i++) {
			existing_letters[i] = false;
		}


		// Load all of the shapes into an array for referencing into an array
		shapes = new Shape[5];
		for (int i = 0; i < 5; i++) {
			shapes[i] = new Shape(i);
		}

		letters = new Letter[26];
		for (int i = 0; i < 26; i++) {
			letters[i] = new Letter(i);
		}

		lastfinger1[0] = -1;
		lastfinger1[1] = -1;
		lastfinger2[0] = -1;
		lastfinger2[1] = -1;

		stage = 0;
		

	
		
		try {
			
			Camera mCamera = Camera.open(); // attempt to get a Camera instance	
			Parameters parameters = mCamera.getParameters();
			
			//set the preview size to be wholly contained in the view
			List<Size> preview_sizes = parameters.getSupportedPreviewSizes();
			Iterator it = preview_sizes.iterator();
	    	while(it.hasNext()){
	    		Size s = (Size) it.next();
	    		if(s.width <= screen_size.x && s.height <= screen_size.y)
	    			preview_size.set(s.width, s.height);			
	    	}
			Log.d("Tap", "Selectcted Preview Size: "+Globals.preview_size.x+" "+Globals.preview_size.y);

			Globals.aspect_ratio = (double)Globals.preview_size.x/(double)Globals.preview_size.y;
			Log.d("Tap", "Aspect Ratio: "+Globals.aspect_ratio);
	    	
	    	//get the largest format that has the same aspect ratio
			List<Size> picture_sizes = parameters.getSupportedPictureSizes();
			it = picture_sizes.iterator();
	    	while(it.hasNext()){
	    		Size s = (Size) it.next();
	    		if(Math.abs(((double)s.width / (double)s.height) - Globals.aspect_ratio) < .001)
	    			Globals.picture_size.set(s.width, s.height);		

	    	}
	    	
	    	
	    	//get the largest format that has the same aspect ratio
			List<Size> video_sizes = parameters.getSupportedVideoSizes();
			it = video_sizes.iterator();
	    	while(it.hasNext()){
	    		Size s = (Size) it.next();
	    		if(s.width <= screen_size.x && s.height <= screen_size.y)
	    			video_size.set(s.width, s.height);	

	    	}
			Log.d("Tap", "Selectcted Video Size: "+Globals.video_size.x+" "+Globals.video_size.y);	
			Log.d("Tap", "Selectcted Picture Size: "+Globals.picture_size.x+" "+Globals.picture_size.y);	
	    	Size vs = parameters.getPreferredPreviewSizeForVideo();		
			Globals.video_preview_size.set(vs.width, vs.height);
			
			mCamera.release();

		} catch (Exception e) {
			Log.d("Tap", "No Camera Exists");	

		}
	

		
		
  
		

	}
	
	public static void addNewThread(Thread t){
		updateActiveThreads(); //must be called before this to make sure that all inactive threds are removed
		
		if(threads.isEmpty()) t.start();
		threads.add(t);

	}
	
	//returns whether or not there are running threads
	public static boolean updateActiveThreads(){
		
		Thread t = (Thread) threads.peek();
		if(t == null) return false;
		
		if(!t.isAlive()){
			threads.poll();
			t = (Thread) threads.peek();
			if(t != null) t.start();
		}
		
		return !threads.isEmpty();
			
		
	}
	
	public static boolean createNewDirectory(String time){
		timeStamp =time;
		mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
				"SI_" + timeStamp);
		
		base_dir_name = "SI_" + timeStamp;
		//base_dir_name = "testdir";
	

		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("MyCameraApp", "failed to create directory");
				return false;
			}
		}
		return true;
	}
	
	public static boolean renameDirectory(String s){
		boolean success = false;
		File file = new File(getTestPath());
		File file2 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),s);
		if(!file2.exists()){
		 success = file.renameTo(file2);
		}
		
		if(success) base_dir_name = s;
		return success;
	}
	
	public static boolean directoryHasAnyVideos(String dir){
		
		//return getPath();
		File testDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
				dir);
		String path;
		if(testDir.exists()) path = testDir.getPath();
		else return false;
			
	
		for(int i = 0; i < 5; i++){
			String stage_path  = path + File.separator + "VID_" + Integer.toString(i) + ".mp4";
			File vidFile = new File(stage_path);
			return vidFile.exists();
		}
		
		return false;
	}
	
	public static boolean hasAnyVideos(){
		for(int i = 0; i < 5; i++){
			if(stageHasVideo(i)) return true;
		}
		return false;
	}

	public static boolean stageHasVideo(int stageid) {
		
		File vidFile = new File(getStageVideoPath(stageid));
		return vidFile.exists();

	}
	

	public static String getStageVideoPath(int stageid) {
		return (getTestPath() + File.separator + "VID_" + Integer.toString(stageid) + ".mp4");
	}

	public static String getStagePhotoPath(int stageid) {
		return (getTestPath() + File.separator + "IMG_" + Integer.toString(stageid) + ".jpg");
	}

	
	public static boolean stageHasVideo() {
		
		File vidFile = new File(getStageVideoPath());
		return vidFile.exists();

	}
	
	public static String getStageVideoPath() {
		if(playback_mode) return (getTestPath() + File.separator + "VID_" + Integer.toString(force_stage) + ".mp4");
		else return (getTestPath() + File.separator + "VID_" + Integer.toString(stage) + ".mp4");
	}

	public static boolean buildLetters(int stage) {

		Log.d("Thead", "Enter Build Letters "+stage);
		
		int w = letter_size; // this is based on 2 * the bounding box size
		int h = letter_size;
		int[] offset;
		Bitmap bmap;
		boolean make_letter;

		for (int i = 0; i < letters.length; i++) {

			make_letter = true;

			// make sure we have the shapes and are able to create this
			int[] shape_ids = letters[i].getShapeIds();
			for (int k = 0; k < shape_ids.length; k++) {
				if (shape_ids[k] > stage) {
					make_letter = false;
					break;
				}
			}

			if (make_letter && !existing_letters[i]) {
				
				existing_letters[i] = true;
				Log.d("Thead", "Make Letter "+intToChar(i));

				
				Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
				Canvas c = new Canvas(bitmap);

				c.drawColor(Color.TRANSPARENT);
				int[] x_points = letters[i].getXPoints();
				int[] y_points = letters[i].getYPoints();
				float[] rots = letters[i].getRotations();

				for (int j = 0; j < x_points.length; j++) {
					
					offset = shapes[shape_ids[j]].getOffset();
					c.save();

					c.translate(x_points[j] * Globals.shapeStretch, y_points[j]
							* Globals.shapeStretch);
					c.rotate((int) Math.toDegrees(rots[j]));
					c.translate(offset[0] * Globals.shapeStretch, offset[1]
							* Globals.shapeStretch);

					File f = new File(getTestPath()
							+ File.separator + "IMG_"
							+ Integer.toString(shape_ids[j]) + "_CROP.png");
					
					Log.d("Thead", "File Exists "+f.exists()+ "Path: "+f.getPath());

					
					
					bmap = Globals.decodeSampledBitmapFromResource(f,letter_size, letter_size);
				
					Log.d("Thead", "Bmap "+bmap);
					if(bmap == null) Log.d("Thead", "Bmap Nulls Ville");

					
					c.drawBitmap(bmap, new Matrix(), null);
					c.restore();

				}

				try {

					Bitmap out = Bitmap.createBitmap(bitmap, 0, 0, w, h,
							new Matrix(), false);
					File pictureFile = Globals.getOutputMediaFile(
							MEDIA_TYPE_IMAGE, intToChar(i) + ".png");
					if (pictureFile == null) {
						return false;
					}

					try {
						FileOutputStream fos = new FileOutputStream(pictureFile);
						out.compress(Bitmap.CompressFormat.PNG, 100, fos);
						fos.close();

					} catch (FileNotFoundException e) {
						Log.d("Canvas", "File not found: " + e.getMessage());
					} catch (IOException e) {
						Log.d("Canvas",
								"Error accessing file: " + e.getMessage());
					}

				} catch (IllegalArgumentException e) {
					Log.d("Canvas", "Illegal Arg" + e.getMessage());
				}

			}
		}
		
		Log.d("Thead", "Exit Build Letters");
		return true;
	}
	
	

	public static Shape getStageShape() {
		return shapes[stage];
	}

	public static void nextStage() {
		stage++;
	}

	public static String getTime() {
		return timeStamp;
	}

//	public static String getPath() {
//		return mediaStorageDir.getPath();
//	}
	
	public static String getPicturesPath() {
		
		File picturesPath = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),"Camera");
		
//		if (!mediaStorageDir.exists()) {
//			if (!mediaStorageDir.mkdirs()) {
//				Log.d("MyCameraApp", "failed to create directory");
//			}
//		}
		
		return picturesPath.getPath();
	}

	public static String getTestPath() {
		//return getPath();
		if(base_dir_name != null){
		File testDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
				base_dir_name);
		if(testDir.exists()) return testDir.getPath();
		}
		
		return null;	
	}
	
	public static String getBasePath() {
		File testDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
				"");
		return testDir.getPath();
		
		
	}

	
	
	public static Shape getShape(int id) {
		return shapes[id];
	}

	public static Letter getLetter(int id) {
		return letters[id];
	}

	/** Create a File for saving an image or video */
	public static File getOutputMediaFile(int type, String s) {

		// Create a media file name
		File mediaFile;
		mediaFile = new File(getTestPath() + File.separator + s);

		return mediaFile;
	}
	
	public static File getOutputPicturesFile(int type, String s) {

		// Create a media file name
		File mediaFile;
		mediaFile = new File(getPicturesPath() + File.separator + s);

		return mediaFile;
	}

	public static String intToChar(int i) {
		char c = (char) (i + 65); // cast from int to char
		return String.valueOf(c);
	}

	public static void resetStage() {
		stage = 0;
	}

	public static float sqrdist(float x, float y, float xx, float yy) {
		return (float) (Math.pow(xx - x, 2) + Math.pow(yy - y, 2));
	}

	public static float getRotation(float x, float y, float x2, float y2) {

		float[] finger1 = new float[2];
		float[] finger2 = new float[2];

		if (lastfinger1[0] == -1
				|| sqrdist(x, y, lastfinger1[0], lastfinger1[1]) <= sqrdist(x,
						y, lastfinger2[0], lastfinger2[1])) {
			finger1[0] = x;
			finger1[1] = y;
			finger2[0] = x2;
			finger2[1] = y2;
		} else {
			finger1[0] = x2;
			finger1[1] = y2;
			finger2[0] = x;
			finger2[1] = y;
		}

		float dx = finger2[0] - finger1[0];
		float dy = finger2[1] - finger2[0];

		float angle = (float) Math.toDegrees(Math.atan(dy / dx));
		Log.d("Matrix", "Returning Angle " + angle);

		lastfinger1 = finger1;
		lastfinger2 = finger2;

		return angle;
	}

	public static float getScale(float x, float y, float x2, float y2) {

		// get the distance
		double hyp = 600 * Math.sqrt(2);
		double d = Math.sqrt(Math.pow(x2 - x, 2) + Math.pow(y2 - y, 2));

		float ratio = (float) ((float) d / hyp);

		if (ratio > 1.0)
			return 1.0f;
		if (ratio < 0.1)
			return 0.1f;
		return ratio;
	}

	public static float[] getCenter(float x, float y, float x2, float y2) {
		float[] center = new float[2];
		Rect r = new Rect((int) x, (int) y, (int) x2, (int) y2);
		center[0] = r.exactCenterX();
		center[1] = r.exactCenterY();

		return center;
	}
	
	public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {
        if (width > height) {
            inSampleSize = Math.round((float)height / (float)reqHeight);
        } else {
            inSampleSize = Math.round((float)width / (float)reqWidth);
        }
    }
    return inSampleSize;
	}
	
	
	public static Bitmap decodeSampledBitmapFromData(byte[] data, int reqWidth, int reqHeight) {
		
		Bitmap b;

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    try{
	    	BitmapFactory.decodeByteArray(data, 0, data.length, options);
	    }catch (Exception e){
	    	Log.d("BITMAP", e.getMessage());
	    }

	    // Calculate inSampleSize

	    
	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    try{
	    	b = BitmapFactory.decodeByteArray(data, 0, data.length, options);
	        return b;
	    }catch (Exception e){
	    	Log.d("BITMAP", e.getMessage());
	    }
	    
	    return null;
	}
	
	
	public static Bitmap decodeSampledBitmapFromResource(File f, int reqWidth, int reqHeight) {
		
		Bitmap b;

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    
	    try{

	    FileInputStream fis = new FileInputStream(f);
        BitmapFactory.decodeStream(fis, null, options);
        fis.close();
        
	    }catch (IOException e){
	    	Log.d("BITMAP", e.getMessage());
	    }

	    options.inJustDecodeBounds = false;
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    try{
	    FileInputStream fis  = new FileInputStream(f);
        b = BitmapFactory.decodeStream(fis, null, options);
        fis.close();
        return b;
	    }catch (IOException e){
	    	Log.d("BITMAP", e.getMessage());
	    }
	    
	    return null;
	}

	public static void changeDirectory(File dir) {
		mediaStorageDir = dir;
	}
	
	
	public static void writeToLog(Context context, String from, String to, double time){
		String data_line = "";
		
		data_line = String.format("%d, %s, %s, %s, %f, %f, %f, %d, %b \n", line_num++, startDateTime, from, to, time, longitude, latitude, stage, true);
		
		if(getTestPath() != null){
		File log = new File(getTestPath() + File.separator + "LOG.txt");
		FileWriter fw;
		try {
			fw = new FileWriter(log, true);
			if(save_string.length() != 0){
				fw.write(save_string);
				save_string = "";
			}
			fw.write(data_line);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		}else{
			save_string += data_line;
		}

		
	
	
	}
	
	


}
