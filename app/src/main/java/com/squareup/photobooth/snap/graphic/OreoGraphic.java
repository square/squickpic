package com.squareup.photobooth.snap.graphic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;
import com.squareup.photobooth.R;
import com.squareup.photobooth.snap.Scaler;

public class OreoGraphic implements FaceGraphic {

  private final Bitmap oreoBitmap;
  private final RectF eyeRect;
  private PointF leftEye;
  private PointF rightEye;
  private Face face;

  public OreoGraphic(Context context) {
    oreoBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.oreo);
    eyeRect = new RectF();
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

  @Override public void draw(Canvas canvas, Scaler scaler) {
    if (face == null) {
      return;
    }
    float faceWidth = scaler.scaleHorizontal(face.getWidth());
    float eyeSize = faceWidth / 5f;

    drawEye(canvas, scaler, leftEye, eyeSize);
    drawEye(canvas, scaler, rightEye, eyeSize);
  }

  @Override public void forgetFace() {
    leftEye = null;
    rightEye = null;
    face = null;
  }

  private void drawEye(Canvas canvas, Scaler translator, PointF eye, float eyeSize) {
    if (eye == null) {
      return;
    }
    float centerX = translator.translateX(eye.x);
    float centerY = translator.translateY(eye.y);
    float halfEyeSize = eyeSize / 2;
    eyeRect.left = centerX - halfEyeSize;
    eyeRect.top = centerY - halfEyeSize;
    eyeRect.right = eyeRect.left + eyeSize;
    eyeRect.bottom = eyeRect.top + eyeSize;

    canvas.drawBitmap(oreoBitmap, null, eyeRect, null);
  }
}
