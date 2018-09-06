package com.squareup.photobooth.snap.graphic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import com.google.android.gms.vision.face.Face;
import com.squareup.photobooth.R;
import com.squareup.photobooth.snap.Scaler;

public class JakeGraphic implements FaceGraphic {

  private final Bitmap bitmap;
  private final Matrix matrix;
  private Face face;

  public JakeGraphic(Context context) {
    bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.jake);
    matrix = new Matrix();
  }

  @Override public void updateFace(Face face) {
    this.face = face;
  }

  @Override public void draw(Canvas canvas, Scaler translator) {
    if (face == null) {
      return;
    }

    PointF facePosition = face.getPosition();

    float faceX = translator.translateX(facePosition.x);
    float faceY = translator.translateY(facePosition.y);

    float faceWidth = translator.scaleHorizontal(face.getWidth());
    float faceHeight = translator.scaleHorizontal(face.getHeight());

    float jakeWidth = faceWidth;
    float jakeHeight = faceHeight;

    float jakeX = faceX + ((faceWidth - jakeHeight) / 2);
    if (translator.isFrontFacing()) {
      jakeX -= faceWidth;
    }
    float jakeY = faceY;

    matrix.reset();
    float scaleX = (jakeWidth * 1.2f) / bitmap.getWidth();
    float scaleY = (jakeHeight * 1.2f) / bitmap.getHeight();
    matrix.postScale(scaleX, scaleY);
    matrix.postTranslate(jakeX, jakeY);
    float angle = face.getEulerZ();
    if (!translator.isFrontFacing()) {
      angle = -angle;
    }
    matrix.postRotate(angle, jakeX + jakeHeight / 2, jakeY + jakeHeight / 2);

    canvas.drawBitmap(bitmap, matrix, null);
  }

  @Override public void forgetFace() {
    face = null;
  }
}
