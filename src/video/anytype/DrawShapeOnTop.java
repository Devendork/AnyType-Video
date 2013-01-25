package firstsubtext.subtext;

import java.io.File;

import data.Shape;
import firstsubtext.subtext.R.id;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.FontMetrics;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.widget.Button;

class DrawShapeOnTop extends View {

	private Shape shape;
	private boolean confirm;
	private Matrix image_scale;
	int[] x_points;
	int[] y_points;
	Bitmap bmap;
	private boolean custom;
	private Path basePath;
	private Path path;
	private Path custom_path;
	private float[] first;

	private RectF pathBounds;

	public DrawShapeOnTop(Context context, Shape s, boolean c) {
		super(context);
		shape = s;
		confirm = c;
		this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		
		x_points = s.getXPoints();
		y_points = s.getYPoints();
		
		//set the image scaling
		image_scale = new Matrix();
		image_scale.setScale(0.35f, 0.35f); // change this later
		
//		File f = new File(Globals.getPath() + File.separator +"IMG_"+ Integer.toString(shape.getId()) + ".jpg");
//		bmap = Globals.decodeSampledBitmapFromResource(f,this.getWidth(), t);
//		
		 bmap = BitmapFactory.decodeFile(Globals.getTestPath() + File.separator +
				"IMG_"+ Integer.toString(shape.getId()) + ".jpg");
		
		 path = new Path();
			
			path.moveTo(x_points[0], y_points[0]);
			for (int i = 1; i < x_points.length; i++)
				path.lineTo(x_points[i], y_points[i]);
			path.lineTo(x_points[0], y_points[0]);
			
			try {
				Matrix m = new Matrix();
				m.setScale(Globals.shapeStretch, Globals.shapeStretch);
				path.transform(m);
			} catch (Exception e) {
				Log.d("Offset", "Matrix " + e.getMessage());
			}
		
			//compute the bounds
			pathBounds = new RectF();
			boolean  exact = true;
			path.computeBounds(pathBounds, exact);
			
			basePath = new Path(path);
		 
	}

	@Override
	protected void onDraw(Canvas canvas) {

		Paint paint = new Paint();
		Rect image_bound = new Rect(this.getTop(), this.getLeft(), this.getWidth(), this.getHeight());
		Rect shape_bound = shape.getBounds();
		
		Log.d("Bounds", shape_bound.top+", "+shape_bound.left+" "+shape_bound.right+", "+shape_bound.bottom);
		
		if(confirm){
			canvas.drawBitmap(bmap, null,image_bound, null);
			canvas.drawARGB(190, 0, 0, 0);
		}
		
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.WHITE);
		canvas.drawRect(0, 20, 520, 60, paint);

		paint.setTextSize((float) 24);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.BLACK);
		
		if (confirm)
			canvas.drawText("Is this what you wanted?", 20, 50, paint);
		else
			canvas.drawText("Find something that fits into this shape...", 20,
					50, paint);

		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(Color.YELLOW);
		
		if(!confirm || (confirm && !custom)){
			try {
			path.offset((this.getWidth()-pathBounds.width())/2, (this.getHeight()-pathBounds.height())/2);
			} catch (Exception e) {
			Log.d("Offset", e.getMessage());
			}
		}


		if (confirm) {
			canvas.clipPath(path);
			canvas.drawBitmap(bmap, null,image_bound, null);

			if(custom){
				paint.setStyle(Paint.Style.STROKE);
				paint.setColor(Color.CYAN);
				paint.setStrokeWidth(8);
				canvas.drawPath(custom_path, paint);
			}
		}else{
			canvas.drawPath(path, paint);
		}
		super.onDraw(canvas);
	}
	

//	get the bitmap from the screen	
	public Bitmap getShapeImageOut(){
		int offset_x, offset_y;
		
		//get the pixels from the current screen
		Bitmap  bitmap = Bitmap.createBitmap( this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas  c = new Canvas(bitmap);
		c.drawColor(Color.TRANSPARENT);
		
		if(!custom){
			path = new Path(basePath);
		}
		
		//compute the bounds
		pathBounds = new RectF();
		boolean  exact = true;
		basePath.computeBounds(pathBounds, exact);
		
		offset_x = (int)(this.getWidth()-pathBounds.width())/2;
		offset_y = (int)(this.getHeight()-pathBounds.height())/2;
		
		//offset by bounds
		if(!custom){
			try {
				path.offset(offset_x, offset_y);
			} catch (Exception e) {
				Log.d("Offset", e.getMessage());
			}
		}

		//set the background to transparent
	    Rect image_bound = new Rect(this.getTop(), this.getLeft(), this.getWidth(), this.getHeight());
		c.clipPath(path);
		c.drawBitmap(bmap, null,image_bound, null);			
		
		//create a cropped version of the bitmap
		Matrix m = new Matrix();
		try{
			Bitmap out = Bitmap.createBitmap(bitmap,offset_x, offset_y, (int) pathBounds.width(), (int) pathBounds.height(), m, false);
			return out;
		}catch(IllegalArgumentException e){
			Log.d("Get Screen Bitmap", e.getMessage());
		}
		return bitmap;
	}
	
	

	public void startPath(float x, float y) {
		path = new Path(basePath);
		pathBounds = new RectF();
		boolean  exact = true;
		path.computeBounds(pathBounds, exact);
		
		try {
			path.offset((this.getWidth()-pathBounds.width())/2, (this.getHeight()-pathBounds.height())/2);
			} catch (Exception e) {
			Log.d("Offset", e.getMessage());
		}
		
		custom = true;
		first = new float[2];
		
		first[0] = x;
		first[1] = y;
		
		custom_path = new Path();
		custom_path.moveTo(x, y);
		
		
	}

	public void endPath(float x, float y) {	
		custom_path.lineTo(first[0], first[1]);
		
		path = new Path(custom_path);
		pathBounds = new RectF();
		boolean  exact = true;
		path.computeBounds(pathBounds, exact);
	}

	public void addPathPoint(float x, float y) {
		custom_path.lineTo(x, y);
	}
	

}
