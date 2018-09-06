package com.squareup.photobooth.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;

public final class Activities {

  public static boolean isAppInLockTaskMode(Activity activity) {
    ActivityManager activityManager =
        (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);

    if (SDK_INT >= M) {
      // For SDK version 23 and above.
      return activityManager.getLockTaskModeState() != ActivityManager.LOCK_TASK_MODE_NONE;
    }

    if (SDK_INT >= LOLLIPOP) {
      // When SDK version >= 21. This API is deprecated in 23.
      return activityManager.isInLockTaskMode();
    }
    return false;
  }

  private Activities() {
    throw new AssertionError();
  }
}
