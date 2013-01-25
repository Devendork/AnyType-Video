package firstsubtext.subtext;

import java.io.File;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

/***
 * This class handles the letters that are drawn on the screen
 * @author lauradevendorf
 *
 */
public class LetterInstance {
	
	private int id;
	private boolean hasFocus;
	private float  scale;
	private float 	rotation;
	private Bitmap bitmap;
	private float[] pos;
	private RectF bounds;
	private Path p;
	private Matrix m;
	
	
	
	public LetterInstance(int id){
		
		this.id = id;
		this.hasFocus = true;
		this.scale = 0.5f;
		this.rotation = 0;
		this.p = new Path();
		
		File f = new File(Globals.getTestPath() + File.separator +Globals.intToChar(id)+".png");
		bitmap = Globals.decodeSampledBitmapFromResource(f,600, 600);

						
		pos = new float[2];
		pos[0] = 0;
		pos[1] = 0;
		
		bounds = new RectF();
		updateVars();
	}
	
	//call this when position is updated, scale or rotation changes
	private void updateVars(){
		Path p = new Path();
		
		p.lineTo(0, bitmap.getHeight());
		p.lineTo(bitmap.getWidth(), bitmap.getHeight());
		p.lineTo(bitmap.getWidth(), 0);
		p.lineTo(0, 0);
		
		p.computeBounds(bounds, false);
		m = new Matrix();
		
		try{
			m.setRotate(rotation, bounds.centerX(), bounds.centerY());
			m.setScale(scale, scale);
		}catch(Exception e){
			Log.d("Matrix", e.getMessage());
		}
		
		p.transform(m, p);
		p.offset(pos[0], pos[1]);
		p.computeBounds(bounds, false);
	}
	
	
	public Matrix getM(){
		return m;
	}
	
	public Path getPath(){
		return p;
	}
	
	public boolean contains(int x, int y){
		return bounds.contains(x, y);
		
	}
	
	
	public float[] getPos() {
		return pos;
	}



	public void setPos(float x, float y) {
		this.pos[0] = x - (bitmap.getWidth()*scale)/2;
		this.pos[1] = y - (bitmap.getHeight()*scale)/2;
		
		//make sure to update the path
		updateVars();
		
	}
	
	public void setScale(float scale2) {
		scale = scale2;
		updateVars();
	}
		
	
	public void setRotations(float rots){
		rotation = rots;
		updateVars();
	}


	public boolean hasFocus() {
		return hasFocus;
	}

	public void setFocus(boolean hasFocus) {
		this.hasFocus = hasFocus;
	}

	public int getId() {
		return id;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public float getScale() {
		return scale;
	}

	public RectF getBounds() {
		return bounds;
	}

	public float getRotations() {
		return rotation;
	}


	
	

}
