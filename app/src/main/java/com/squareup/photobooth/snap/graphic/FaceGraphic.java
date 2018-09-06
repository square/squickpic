package com.squareup.photobooth.snap.graphic;

import android.graphics.Canvas;
import com.google.android.gms.vision.face.Face;
import com.squareup.photobooth.snap.Scaler;

public interface FaceGraphic {
  void draw(Canvas canvas, Scaler translator);

  void updateFace(Face face);

  void forgetFace();
}
