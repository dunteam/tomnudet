package com.dunteam.android.tomnudet;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

public class ConfirmationActivity extends Activity {

	private static final String NO_FACE_DETECTED = "Уучлаарай. Ямар нэг нүүр олдсонгүй";
	private static final String SAVED_FILE = "%s нэртэй зураг утсанд тань хадгалагдлаа";
	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";
	private static final int RADIUS = 40;
	private final int MAX_FACES = 1;
	private Bitmap bmp;
	private boolean isFront;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);		
		setContentView(R.layout.activity_confirmation);
		
		byte[] data = getIntent().getByteArrayExtra("IMAGE");
		isFront = getIntent().getBooleanExtra("ISFRONT", false);
		
    	bmp = processImage(BitmapFactory.decodeByteArray(data, 0, data.length));
    	
    	if(faceCount() == 0)
    	{
    		Intent returnIntent = new Intent();
    		Toast.makeText(getApplicationContext(), NO_FACE_DETECTED, Toast.LENGTH_LONG).show();
    		setResult(RESULT_CANCELED, returnIntent);        
    		finish();
    	}
    	
    	ImageView imageView = (ImageView)findViewById(R.id.imageConfirmation);
    	
    	imageView.setImageBitmap(bmp);

	}

	public void save(View v) {
		Intent returnIntent = new Intent();
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_" + JPEG_FILE_SUFFIX;
		try 
		{
			File mediaFileDirectory = new File(Environment.getExternalStorageDirectory() + File.separator + "DCIM/Camera/");
			mediaFileDirectory.mkdirs();
			File mediaFile = new File(mediaFileDirectory + File.separator + imageFileName);
			FileOutputStream out = new FileOutputStream(mediaFile);
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.close();
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ mediaFile)));
			Toast.makeText(getApplicationContext(), String.format(SAVED_FILE, imageFileName), Toast.LENGTH_LONG).show();			
		} catch (Exception e) {
		       e.printStackTrace();
		}		
		setResult(RESULT_OK, returnIntent);        
		finish();
	}
	
	public void cancel(View v) {
		Intent returnIntent = new Intent();
		setResult(RESULT_CANCELED, returnIntent);        
		finish();
	}
	
	private int faceCount(){
		FaceDetector.Face[] faces = new FaceDetector.Face[MAX_FACES];
	    FaceDetector fd = new FaceDetector(bmp.getWidth(), bmp.getHeight(), MAX_FACES);
	    return fd.findFaces(bmp, faces);
	}
	
	private Bitmap processImage(Bitmap bmp){
		Bitmap result;
		Matrix rotateMatrix = new Matrix();
		if(isFront)
			rotateMatrix.postRotate(270);
		else
			rotateMatrix.postRotate(90);
		
		result = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), rotateMatrix, false);
		
		return makeItFunny(result);
	}
	
	public Bitmap makeItFunny(Bitmap bmpOriginal)
	{        
	    int width, height;
	    height = bmpOriginal.getHeight();
	    width = bmpOriginal.getWidth();
	    

	    Bitmap withFaces = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
	    Canvas c = new Canvas(withFaces);

	    c.drawBitmap(bmpOriginal, 0, 0, new Paint());
	    
	    FaceDetector.Face[] faces = new FaceDetector.Face[MAX_FACES];
	    PointF midpoint = new PointF();
	    float [] fpx = null;
	    float [] fpy = null;
	    
	    Paint myPaint = new Paint();
	    myPaint.setStyle(Paint.Style.STROKE);
	    myPaint.setColor(Color.RED);
	    
	    Filters filters = new Filters();

		try 
		{
		    FaceDetector fd = new FaceDetector(width, height, MAX_FACES);
		    int count = fd.findFaces(withFaces, faces);

		    float eyedist;
		    if (count > 0) {
		    	 fpx = new float[count];
		    	 fpy = new float[count];
	
			    for (int i = 0; i < count; i++) {
	
						faces[i].getMidPoint(midpoint);
						fpx[i] = midpoint.x;
						fpy[i] = midpoint.y;
						eyedist = faces[i].eyesDistance();

						Bitmap temp;
						temp = filters.barrel(withFaces, fpx[i] - eyedist / 2, fpy[i], RADIUS);						
						temp = filters.barrel(temp, fpx[i] + eyedist / 2, fpy[i], RADIUS);
						
						//Toirgoos gadnah hesgiig tseverlej bui ni.
						//Toirgiin tuv ni mesh-iin ogtoltsol tseguud deer buuh albagui tul
						//tornii distort hiigdsen heseg toirog bolj haragdahgui
						for(int y = 0; y < withFaces.getHeight(); y++)
							for(int x = 0; x < withFaces.getWidth(); x++)
								if(
										Filters.distance(x, y, fpx[i] - eyedist / 2, fpy[i]) < RADIUS ||
										Filters.distance(x, y, fpx[i] + eyedist / 2, fpy[i]) < RADIUS)
								{
									withFaces.setPixel(x, y, temp.getPixel(x, y));
								}
			    }
		 	}
		}
	 	catch (Exception e)
	 	{
	 		Log.e("makeItFunny", e.toString());
	 	}
		
	    return withFaces;
	}			
}
