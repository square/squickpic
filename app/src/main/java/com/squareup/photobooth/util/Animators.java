package com.squareup.photobooth.util;

import android.animation.Animator;

public final class Animators {

  public interface AnimatorEndListener {
    void onAnimationEnd(Animator animator);
  }

  public static Animator.AnimatorListener onAnimationEnd(AnimatorEndListener listener) {
    return new AnimationListenerAdapter() {
      @Override public void onAnimationEnd(Animator animator) {
        listener.onAnimationEnd(animator);
      }
    };
  }

  private Animators() {
    throw new AssertionError();
  }
}
