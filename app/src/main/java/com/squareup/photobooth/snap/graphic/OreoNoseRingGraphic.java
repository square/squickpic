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

public class OreoNoseRingGraphic implements FaceGraphic {

  private final Bitmap oreoBitmap;
  private final RectF noseRect;
  private PointF nose;
  private Face face;

  public OreoNoseRingGraphic(Context context) {
    oreoBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.orenose);
    noseRect = new RectF();
  }

  @Override public void updateFace(Face face) {
    for (Landmark landmark : face.getLandmarks()) {
      switch (landmark.getType()) {
        case Landmark.NOSE_BASE:
          nose = landmark.getPosition();
          break;
      }
    }
    this.face = face;
  }

  @Override public void draw(Canvas canvas, Scaler translator) {
    if (face == null || nose == null) {
      return;
    }
    float faceWidth = translator.scaleHorizontal(face.getWidth());
    float ringSize = faceWidth / 7f;
    float centerX = translator.translateX(nose.x);
    float centerY = translator.translateY(nose.y);
    float halfRingSize = ringSize / 2;
    noseRect.left = centerX - halfRingSize;
    noseRect.top = centerY - halfRingSize;
    noseRect.right = noseRect.left + ringSize;
    noseRect.bottom = noseRect.top + ringSize;

    canvas.drawBitmap(oreoBitmap, null, noseRect, null);
  }

  @Override public void forgetFace() {
    nose = null;
    face = null;
  }
}
