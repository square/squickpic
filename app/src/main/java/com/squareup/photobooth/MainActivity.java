package com.squareup.photobooth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import com.squareup.photobooth.start.KioskStartActivity;
import com.squareup.photobooth.printer.PrinterSetupActivity;
import com.squareup.photobooth.settings.SettingsActivity;
import com.squareup.photobooth.settings.SettingsStore;
import com.squareup.photobooth.util.Activities;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

public class MainActivity extends AppCompatActivity {

  private SettingsStore settingsStore;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    settingsStore = App.from(this).settingsStore();

    setContentView(R.layout.setup);

    findViewById(R.id.photobooth_button).setOnClickListener(v -> startPhotobooth());

    findViewById(R.id.printers_button).setOnClickListener(
        v -> startActivity(new Intent(MainActivity.this, PrinterSetupActivity.class)));

    findViewById(R.id.settings_button).setOnClickListener(
        v -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));
  }

  @Override protected void onResume() {
    super.onResume();

    if (settingsStore.shouldLockTask()) {
      if (SDK_INT >= LOLLIPOP) {
        if (!Activities.isAppInLockTaskMode(this)) {
          // This shows a dialog to confirm that we want to go in lock task mode.
          startLockTask();
        }
      } else {
        snack("Kiosk not supported on API " + SDK_INT);
      }
    }
  }

  private void startPhotobooth() {
    startActivity(new Intent(this, KioskStartActivity.class));
  }

  private void snack(String message) {
    Snackbar.make(findViewById(R.id.photobooth_button), message, Snackbar.LENGTH_SHORT).show();
  }
}
