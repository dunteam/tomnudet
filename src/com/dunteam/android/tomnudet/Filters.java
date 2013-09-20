package com.dunteam.android.tomnudet;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Filters {

	private static final int WIDTH = 40;
	private static final int HEIGHT = 40;
	private static final int COUNT = (WIDTH + 1) * (HEIGHT + 1);
	private final float[] mVerts = new float[COUNT * 2];
	private final float[] mOrig = new float[COUNT * 2];
	private float xdiff;
	private float ydiff;

	public Filters() {
	}

	public Bitmap barrel(Bitmap input, float cx, float cy, float R) {

		float w = input.getWidth();
		float h = input.getHeight();

		int index = 0;
		for (int y = 0; y <= HEIGHT; y++) {
			float fy = h * y / HEIGHT;

			for (int x = 0; x <= WIDTH; x++) {
				float fx = w * x / WIDTH;
				setXY(mVerts, index, fx, fy);
				setXY(mOrig, index, fx, fy);
				index += 1;
			}
		}
		
		xdiff = w / WIDTH;
		ydiff = h / HEIGHT;
		
		warp(cx, cy, R);

	    Bitmap dst = Bitmap.createBitmap(input.getWidth(), input.getHeight(), Bitmap.Config.RGB_565);
	    Canvas canvas = new Canvas(dst);
		canvas.drawBitmapMesh(input, WIDTH, HEIGHT, mVerts, 0, null, 0, null);

		return dst;
	}

	private void warp(float cx, float cy, float R) {
		float nx = 2 * R / xdiff;
		float ny = 2 * R / ydiff;

		double alphax = Math.PI / nx;
		double alphay = Math.PI / ny;
		float[] src = mOrig;
		float[] dst = mVerts;
		float xx, yy;		

		for (int i = 0; i < COUNT * 2; i += 2) {
			float x = src[i + 0];
			float y = src[i + 1];

			if(distance(x, y, cx, cy) <= R)
			{
				xx = (x - cx) / xdiff;
				yy = (y - cy) / ydiff;

				dst[i + 0] = (float) (cx + R * Math.cos(alphax * (nx / 2 - xx)));
				dst[i + 1] = (float) (cy + R * Math.cos(alphay * (ny / 2 - yy)));
			}
				else
			{
				dst[i + 0] = x;
				dst[i + 1] = y;
			}						
		}
	}

	public static double distance(float x1, float y1, float x2, float y2) {
		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}
	
	private static void setXY(float[] array, int index, float x, float y) {
		array[index * 2 + 0] = x;
		array[index * 2 + 1] = y;
	}
}