package com.squareup.photobooth.util;

import android.os.Looper;

public final class Preconditions {
  public static void assertMainThread() {
    if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
      throw new UnsupportedOperationException(
          "Expected main thread, not " + Thread.currentThread());
    }
  }

  private Preconditions() {
    throw new AssertionError();
  }
}
