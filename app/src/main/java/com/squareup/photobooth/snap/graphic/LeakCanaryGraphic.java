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

public class LeakCanaryGraphic implements FaceGraphic {

  private final Bitmap bitmap;
  private final RectF rect;
  private PointF leftEye;
  private PointF rightEye;
  private Face face;

  public LeakCanaryGraphic(Context context) {
    bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.leakcanary);
    rect = new RectF();
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
    if (leftEye == null || rightEye == null) {
      return;
    }

    float eyeAverageY = translator.translateY((leftEye.y + rightEye.y) / 2);

    PointF facePosition = face.getPosition();

    // top left, reversed with front camera so top right.
    float faceX = translator.translateX(facePosition.x);
    float faceY = translator.translateY(facePosition.y);

    float faceWidth = translator.scaleHorizontal(face.getWidth());
    float shieldWidth = faceWidth * 0.60f;
    float shieldHeight = (shieldWidth * bitmap.getHeight()) / bitmap.getWidth();

    float shieldX;
    if (translator.isFrontFacing()) {
      shieldX = (faceX - faceWidth / 2) - shieldWidth / 2;
    } else {
      shieldX = (faceX + faceWidth / 2) - shieldWidth / 2;
    }

    // tip of shield is on forefront
    float shieldY = faceY + ((eyeAverageY - faceY) * 0.65f) - shieldHeight;

    rect.left = shieldX;
    rect.top = shieldY;
    rect.right = shieldX + shieldWidth;
    rect.bottom = shieldY + shieldHeight;
    canvas.drawBitmap(bitmap, null, rect, null);
  }

  @Override public void forgetFace() {
    leftEye = null;
    rightEye = null;
    face = null;
  }
}
