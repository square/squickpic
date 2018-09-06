package com.squareup.photobooth.util;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

public abstract class FullscreenActivity extends AppCompatActivity {

  @Override public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    if (hasFocus) {
      setFullscreen();
    }
  }

  @Override protected void onResume() {
    super.onResume();
    setFullscreen();
  }

  private void setFullscreen() {
    getWindow().getDecorView()
        .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
  }
}
