package com.squareup.photobooth.snap.graphic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;
import com.squareup.photobooth.R;
import com.squareup.photobooth.snap.Scaler;

public class PieGraphic implements FaceGraphic {

  private final Bitmap bitmap;
  private final Bitmap reversedBitmap;
  private final RectF eyeRect;
  private final float ratio;
  private PointF leftEye;
  private PointF rightEye;
  private Face face;

  public PieGraphic(Context context) {
    bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pie);
    ratio = (bitmap.getHeight() * 1f) / bitmap.getWidth();
    eyeRect = new RectF();
    Matrix m = new Matrix();
    m.preScale(-1, 1);
    reversedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, false);
  }

  @Override public void updateFace(Face face) {
    for (Landmark landmark : face.getLandmarks()) {
      switch (landmark.getType()) {
        case Landmark.LEFT_EYE:
          leftEye = landmark.getPosition();
          break;
        case Landmark.RIGHT_EYE:
          rightEye = landmark.getPosition();
          break;
      }
    }
    this.face = face;
  }

  @Override public void draw(Canvas canvas, Scaler translator) {
    if (face == null) {
      return;
    }
    float faceWidth = translator.scaleHorizontal(face.getWidth());
    float eyeSize = faceWidth / 5f;

    drawEye(canvas, translator, leftEye, eyeSize);
    drawEye(canvas, translator, rightEye, eyeSize);
  }

  @Override public void forgetFace() {
    leftEye = null;
    rightEye = null;
    face = null;
  }

  private void drawEye(Canvas canvas, Scaler scaler, PointF eye, float eyeSize) {
    float centerX = scaler.translateX(eye.x);
    float centerY = scaler.translateY(eye.y);
    float halfEyeSize = eyeSize / 2;
    float eyeHeight = eyeSize * ratio;

    eyeRect.left = centerX - halfEyeSize;
    eyeRect.top = centerY - halfEyeSize;
    eyeRect.right = eyeRect.left + eyeSize;
    eyeRect.bottom = eyeRect.top + eyeHeight;

    if (scaler.isFrontFacing()) {
      canvas.drawBitmap(reversedBitmap, null, eyeRect, null);
    } else {
      canvas.drawBitmap(bitmap, null, eyeRect, null);
    }
  }
}
