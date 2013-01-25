package firstsubtext.subtext;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;


	//use this to build each letter in the background so it doesn't hang the UI
	class BuildLettersTask extends AsyncTask<Void, Void, Void> {
	    /** The system calls this to perform work in a worker thread and
	      * delivers it the parameters given to AsyncTask.execute() */
		protected Void doInBackground(Void... params) {
	    	Log.d("Thread", "Starting Process");
	    	Globals.buildLetters();
	    	Log.d("Thread", "Finished Process");

			return null;
		}

	    /** The system calls this to perform work in the UI thread and delivers
	      * the result from doInBackground() */
	    protected void onPostExecute() {
	    	Log.d("Thread", "Execute");
	    }

	}	
	

