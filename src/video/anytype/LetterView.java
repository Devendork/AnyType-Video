package firstsubtext.subtext;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import data.Letter;
import data.Shape;
import firstsubtext.subtext.R.id;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Paint.FontMetrics;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

class LetterView extends View {

	private HashMap<Integer, LetterInstance> letters;
	private int cur;
	private int uid = 0;
	

	public LetterView(Context context) {
		super(context);
		cur = -1;
		letters = new HashMap();

	}
	
	public void loadState(LetterView lv){
		this.letters = lv.getLetters();
		this.cur = lv.getCur();
		this.uid = lv.getUid();
	}
	
	
	public HashMap<Integer, LetterInstance> getLetters(){
		return letters;
	}
	
	
	public int  getUid(){
		return uid;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		
		Iterator it = letters.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry me = (Map.Entry) it.next();
			LetterInstance li = (LetterInstance) me.getValue();
			
			float[] pos = li.getPos();
			Bitmap b = li.getBitmap();
			RectF  bounds = li.getBounds();
			
			Matrix mn = new Matrix();
			try{
				Log.d("Matrix", "Rotations is "+li.getRotations());
				//mn.setRotate(li.getRotations(), b.getWidth()/2, b.getHeight()/2);
				mn.setScale(li.getScale(), li.getScale());
			}catch(Exception e){
				
			}
			canvas.save();
			canvas.translate(pos[0], pos[1]);
			canvas.rotate(li.getRotations());
			canvas.drawBitmap(b, mn, null);
			canvas.restore();

			//outline the selected shape
			if(li.hasFocus()){
				Paint p = new Paint();
				p.setColor(Color.YELLOW);
				p.setStyle(Style.STROKE);
				//canvas.rotate(li.getRotations());
				canvas.drawRect(bounds, p);
			}

			
		}
		
		super.onDraw(canvas);
	}
	
	public int getCur(){
		return cur;
	}
	
	public int getCurLetterId(){
		LetterInstance li = letters.get(cur);
		return li.getId();
	}
	
	
	public int locate(int x, int y){
		Iterator it = letters.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry me = (Map.Entry) it.next();
			LetterInstance li = (LetterInstance) me.getValue();
			
			
			
			if(li.contains(x, y)){
				return (Integer) me.getKey();
			}
		}
		return -1;
	}
	
/**
 * Selected the item at key in letters
 * @param id
 */
	public void select(int id){
		LetterInstance li = letters.get(id);
		li.setFocus(true);
		cur = id;
		
	}
	
	public void deselect(int id){
		LetterInstance li = letters.get(id);
		li.setFocus(false);
		cur = -1;
	}
	
	public void removeCurrentLetter(){
		if(cur != -1) letters.remove(cur);
		cur = -1;
	}
	
	public void addLetter(int id){
		letters.put((uid), new LetterInstance(id));
		cur = uid++;
		updatePosition((1280/26)*id, 60);
	}
	
	///this is only to be used in the Letter Video View Class
	public void addSingleLetter(int id){
		letters.put((uid), new LetterInstance(id));
	}
	
	public void updatePosition(float x, float y){
		if(cur == -1) return;
		
		LetterInstance li = letters.get(cur);
		li.setPos(x, y);
	}
	

	public void updateScale(float scale) {
		if(cur == -1) return;
		
		LetterInstance li = letters.get(cur);
		li.setScale(scale);
		
	}
	
	public void setRotations(float rots){
		if(cur == -1) return;
		
		LetterInstance li = letters.get(cur);
		li.setRotations(rots);
		
	}
	
//	get the bitmap from the screen	
	public Bitmap getImageOut(){

		Log.d("BITMAP", "Get Image Out "+this.getWidth()+" "+this.getHeight());
		Bitmap  bitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas  c = new Canvas(bitmap);
		
		Log.d("BITMAP", "onDraw");

		onDraw(c);
		//just copying on draw
		Log.d("BITMAP", "return bitmap");

		//end copy
		return bitmap;
	}

	

	
	

}
