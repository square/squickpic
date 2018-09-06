package com.squareup.photobooth.snap;

public interface Scaler {

  Scaler ISO = new Scaler() {

    @Override public float translateX(float x) {
      return x;
    }

    @Override public float translateY(float y) {
      return y;
    }

    @Override public float scaleHorizontal(float width) {
      return width;
    }

    @Override public float scaleVertical(float height) {
      return height;
    }

    @Override public boolean isFrontFacing() {
      return false;
    }
  };

  /** Use for x positions */
  float translateX(float x);

  /** Use for y positions */
  float translateY(float y);

  float scaleHorizontal(float width);

  float scaleVertical(float height);

  boolean isFrontFacing();
}
