package com.squareup.photobooth.snap.graphic;

import android.content.Context;
import android.support.annotation.DrawableRes;
import com.squareup.photobooth.R;

public enum GraphicFactory {

  PIE(R.drawable.pie) {
    @Override public FaceGraphic create(Context context) {
      return new PieGraphic(context);
    }
  },

  ORENOSE(R.drawable.orenose) {
    @Override public FaceGraphic create(Context context) {
      return new OreoNoseRingGraphic(context);
    }
  },

  LEAK_CANARY(R.drawable.leakcanary) {
    @Override public FaceGraphic create(Context context) {
      return new LeakCanaryGraphic(context);
    }
  },

  JAKE(R.drawable.jake) {
    @Override public FaceGraphic create(Context context) {
      return new JakeGraphic(context);
    }
  },

  OREO(R.drawable.oreo) {
    @Override public FaceGraphic create(Context context) {
      return new OreoGraphic(context);
    }
  },

  ROBOT(R.drawable.robot) {
    @Override public FaceGraphic create(Context context) {
      return new RobotGraphic(context);
    }
  },
  //
  ;

  public final @DrawableRes int drawableResId;

  GraphicFactory(@DrawableRes int drawableResId) {
    this.drawableResId = drawableResId;
  }

  public abstract FaceGraphic create(Context context);
}
