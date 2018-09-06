package com.squareup.photobooth.snap.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import com.squareup.photobooth.R;

public class CameraButton extends View {

  private final Paint outerPaint;
  private final Paint innerPaint;
  private final int strokeWidth;

  public CameraButton(Context context, AttributeSet attrs) {
    super(context, attrs);
    strokeWidth = getResources().getDimensionPixelSize(R.dimen.camera_button_stroke);

    outerPaint = new Paint();
    outerPaint.setColor(0xffffffff);
    outerPaint.setStyle(Paint.Style.STROKE);
    outerPaint.setStrokeWidth(strokeWidth);
    outerPaint.setAntiAlias(true);

    innerPaint = new Paint();
    innerPaint.setColor(context.getResources().getColor(R.color.reader_blue));
    innerPaint.setStyle(Paint.Style.STROKE);
    innerPaint.setStrokeWidth(strokeWidth / 2f);
    innerPaint.setAntiAlias(true);
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    boolean handled = super.onTouchEvent(event);
    if (handled) {
      switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
          animate().scaleX(0.5f)
              .scaleY(0.5f)
              .setInterpolator(new AccelerateDecelerateInterpolator());
          break;
        case MotionEvent.ACTION_UP:
          animate().scaleX(1f).scaleY(1f).setInterpolator(new AccelerateDecelerateInterpolator());
          break;
      }
    }
    return handled;
  }

  @Override protected void onDraw(Canvas canvas) {
    int width = getWidth();
    float centerX = width / 2f;
    int height = getHeight();
    float centerY = height / 2f;
    float radius = Math.min(width, height) / 2 - strokeWidth;
    canvas.drawCircle(centerX, centerY, radius, outerPaint);
    canvas.drawCircle(centerX, centerY, radius, innerPaint);
  }
}
