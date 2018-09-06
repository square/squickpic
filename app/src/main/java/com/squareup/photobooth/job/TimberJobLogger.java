package com.squareup.photobooth.job;

import com.birbit.android.jobqueue.log.CustomLogger;
import com.squareup.photobooth.BuildConfig;
import timber.log.Timber;

public class TimberJobLogger implements CustomLogger {
  @Override public boolean isDebugEnabled() {
    return BuildConfig.DEBUG;
  }

  @Override public void d(String text, Object... args) {
    Timber.d(text, args);
  }

  @Override public void e(Throwable t, String text, Object... args) {
    Timber.d(t, text, args);
  }

  @Override public void e(String text, Object... args) {
    Timber.d(text, args);
  }

  @Override public void v(String text, Object... args) {
    Timber.d(text, args);
  }
}
