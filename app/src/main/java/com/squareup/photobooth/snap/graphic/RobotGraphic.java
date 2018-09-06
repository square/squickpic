package com.squareup.photobooth.snap.graphic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;
import com.squareup.photobooth.R;
import com.squareup.photobooth.snap.Scaler;

public class RobotGraphic implements FaceGraphic {

  private final Bitmap oreoBitmap;
  private final Matrix matrix;
  private PointF leftEye;
  private PointF rightEye;
  private Face face;

  public RobotGraphic(Context context) {
    oreoBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.robot);
    matrix = new Matrix();
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

    PointF facePosition = face.getPosition();

    float faceX = translator.translateX(facePosition.x);
    float faceY = translator.translateY(facePosition.y);

    float faceWidth = translator.scaleHorizontal(face.getWidth());

    float centerY = translator.translateY((leftEye.y + rightEye.y) / 2);

    float robotHeadHeightRatio = 0.813f;
    float robotToHumanWidthRatio = 0.8f;

    float robotWidth = faceWidth * robotToHumanWidthRatio;
    float robotHeight = (centerY - faceY) / robotHeadHeightRatio;

    float robotX = faceX + ((faceWidth - robotWidth) / 2);
    if (translator.isFrontFacing()) {
      robotX -= faceWidth;
    }
    float robotY = faceY;

    matrix.reset();
    float scaleX = (robotWidth * 1f) / oreoBitmap.getWidth();
    float scaleY = (robotHeight * 1f) / oreoBitmap.getHeight();
    matrix.postScale(scaleX, scaleY);
    matrix.postTranslate(robotX, robotY);
    float angle = face.getEulerZ();
    if (!translator.isFrontFacing()) {
      angle = -angle;
    }
    matrix.postRotate(angle, robotX + robotWidth / 2, robotY + robotHeight / 2);

    canvas.drawBitmap(oreoBitmap, matrix, null);

  }

  @Override public void forgetFace() {
    leftEye = null;
    rightEye = null;
    face = null;
  }
}
