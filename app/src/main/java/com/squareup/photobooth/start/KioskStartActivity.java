package com.squareup.photobooth.start;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.squareup.photobooth.App;
import com.squareup.photobooth.BuildConfig;
import com.squareup.photobooth.R;
import com.squareup.photobooth.settings.SettingsStore;
import com.squareup.photobooth.settings.StartAuthorizeActivity;
import com.squareup.photobooth.snap.SnapActivity;
import com.squareup.photobooth.util.Activities;
import com.squareup.photobooth.util.FullscreenActivity;
import com.squareup.sdk.reader.ReaderSdk;
import com.squareup.sdk.reader.authorization.AuthorizationManager;
import com.squareup.sdk.reader.checkout.CheckoutErrorCode;
import com.squareup.sdk.reader.checkout.CheckoutManager;
import com.squareup.sdk.reader.checkout.CheckoutParameters;
import com.squareup.sdk.reader.checkout.CheckoutResult;
import com.squareup.sdk.reader.checkout.CurrencyCode;
import com.squareup.sdk.reader.checkout.Money;
import com.squareup.sdk.reader.core.CallbackReference;
import com.squareup.sdk.reader.core.Result;
import com.squareup.sdk.reader.core.ResultError;
import timber.log.Timber;

public class KioskStartActivity extends FullscreenActivity {

  private CallbackReference checkoutCallbackRef;

  private boolean waitingForActivityStart = false;
  private SettingsStore settingsStore;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.kiosk_start_activity);

    settingsStore = App.from(this).settingsStore();

    CheckoutManager checkoutManager = ReaderSdk.checkoutManager();
    checkoutCallbackRef = checkoutManager.addCheckoutActivityCallback(this::onCheckoutResult);

    View entireScreen = findViewById(R.id.entire_screen);
    entireScreen.setOnClickListener(view -> onScreenTapped());
  }

  private void onScreenTapped() {
    AuthorizationManager authorizationManager = ReaderSdk.authorizationManager();
    boolean readerSdkAuthorized = authorizationManager.getAuthorizationState().isAuthorized();
    if (settingsStore.arePaymentsEnabled() && readerSdkAuthorized) {
      startCheckout();
    } else {
      startActivity(new Intent(this, SnapActivity.class));
    }
  }

  private void startCheckout() {
    if (waitingForActivityStart) {
      return;
    }
    waitingForActivityStart = true;
    Money checkoutAmount = new Money(settingsStore.getPaymentsAmount(), CurrencyCode.current());
    CheckoutManager checkoutManager = ReaderSdk.checkoutManager();
    CheckoutParameters.Builder params = CheckoutParameters.newBuilder(checkoutAmount)
        .skipReceipt(true)
        .alwaysRequireSignature(false)
        .note("Smile! 📸");
    checkoutManager.startCheckoutActivity(this, params.build());
  }

  private void onCheckoutResult(Result<CheckoutResult, ResultError<CheckoutErrorCode>> result) {
    if (result.isSuccess()) {
      startActivity(new Intent(getBaseContext(), SnapActivity.class));
    } else {
      ResultError<CheckoutErrorCode> error = result.getError();

      switch (error.getCode()) {
        case SDK_NOT_AUTHORIZED:
          authorizeReaderSdk();
          break;
        case CANCELED:
          Toast.makeText(this, R.string.checkout_canceled_toast, Toast.LENGTH_SHORT).show();
          break;
        case USAGE_ERROR:
          showErrorDialog(error);
          break;
      }
    }
  }

  private void authorizeReaderSdk() {
    if (waitingForActivityStart) {
      return;
    }
    waitingForActivityStart = true;
    Intent intent = new Intent(this, StartAuthorizeActivity.class);
    startActivity(intent);
    finish();
  }

  private void showErrorDialog(ResultError<?> error) {
    String dialogMessage = error.getMessage();
    if (BuildConfig.DEBUG) {
      dialogMessage += "\n\nDebug Message: " + error.getDebugMessage();
      Timber.d("%s: %s, %s", error.getCode(), error.getDebugCode(), error.getDebugMessage());
    }
    showDialog(getString(R.string.error_dialog_title), dialogMessage);
  }

  private void showDialog(CharSequence title, CharSequence message) {
    new AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(R.string.ok,
            (dialogInterface, i) -> startActivity(new Intent(getBaseContext(), SnapActivity.class)))
        .setOnDismissListener(
            dialogInterface -> startActivity(new Intent(getBaseContext(), SnapActivity.class)))
        .show();
  }

  @Override protected void onResume() {
    super.onResume();
    waitingForActivityStart = false;
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    checkoutCallbackRef.clear();
  }

  @Override public void onBackPressed() {
    if (Activities.isAppInLockTaskMode(this)) {
      Toast.makeText(this, R.string.app_task_locked, Toast.LENGTH_SHORT).show();
      return;
    }
    super.onBackPressed();
  }
}
