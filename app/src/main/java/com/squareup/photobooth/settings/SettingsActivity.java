package com.squareup.photobooth.settings;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import com.squareup.photobooth.App;
import com.squareup.photobooth.BuildConfig;
import com.squareup.photobooth.R;
import com.squareup.sdk.reader.ReaderSdk;
import com.squareup.sdk.reader.authorization.AuthorizationManager;
import com.squareup.sdk.reader.authorization.AuthorizationState;
import com.squareup.sdk.reader.authorization.DeauthorizeErrorCode;
import com.squareup.sdk.reader.core.CallbackReference;
import com.squareup.sdk.reader.core.ResultError;
import com.squareup.sdk.reader.hardware.ReaderManager;
import com.squareup.sdk.reader.hardware.ReaderSettingsErrorCode;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

public class SettingsActivity extends AppCompatActivity {

  private static final String TAG = SettingsActivity.class.getSimpleName();

  private TwitterLoginButton twitterLoginButton;
  private View twitterLogoutButton;
  private TextView twitterStateView;
  private EditText tweetMessageView;
  private SettingsStore settingsStore;
  private View readerSdkLoginButton;
  private View readerSdkLogoutButton;
  private TextView readerSdkLoginStateView;

  private boolean waitingForActivityStart = false;

  private CallbackReference deauthorizeCallbackRef;
  private CallbackReference readerSettingsCallbackRef;
  private EditText paymentAmountEdittext;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.settings);

    AuthorizationManager authorizationManager = ReaderSdk.authorizationManager();
    deauthorizeCallbackRef = authorizationManager.addDeauthorizeCallback(this::onDeauthorizeResult);
    ReaderManager readerManager = ReaderSdk.readerManager();
    readerSettingsCallbackRef =
        readerManager.addReaderSettingsActivityCallback(this::onReaderSettingsResult);

    App app = App.from(this);
    settingsStore = app.settingsStore();
    twitterLoginButton = findViewById(R.id.twitter_login_button);
    twitterLogoutButton = findViewById(R.id.twitter_logout_button)  ;
    twitterLoginButton.setCallback(new Callback<TwitterSession>() {
      @Override public void success(Result<TwitterSession> result) {
        snack("Twitter login success");
        updateTwitterState();
      }

      @Override public void failure(TwitterException exception) {
        snack("Twitter login failure");
        updateTwitterState();
      }
    });
    twitterLogoutButton.setOnClickListener(view -> {
      TwitterCore.getInstance().getSessionManager().clearActiveSession();
      updateTwitterState();
    });

    CheckBox kioskCheckbox = findViewById(R.id.kiosk_check);
    kioskCheckbox.setChecked(settingsStore.shouldLockTask());
    kioskCheckbox.setOnCheckedChangeListener(
        (compoundButton, checked) -> settingsStore.setLockTask(checked));

    CheckBox paymentsCheckbox = findViewById(R.id.payments_check);
    paymentsCheckbox.setChecked(settingsStore.arePaymentsEnabled());
    paymentsCheckbox.setOnCheckedChangeListener(
        (compoundButton, checked) -> settingsStore.setPaymentsEnabled(checked));

    CheckBox httpLogCheckbox = findViewById(R.id.http_log_check);
    httpLogCheckbox.setChecked(settingsStore.shouldLogHttp());
    httpLogCheckbox.setOnCheckedChangeListener(
        (compoundButton, checked) -> settingsStore.setLogHttp(checked));

    paymentAmountEdittext = findViewById(R.id.payments_amount);
    paymentAmountEdittext.setText(Integer.toString(settingsStore.getPaymentsAmount()));

    tweetMessageView = findViewById(R.id.tweet_message);
    tweetMessageView.setText(settingsStore.getTweetMessage());
    twitterStateView = findViewById(R.id.twitter_state);
    updateTwitterState();

    readerSdkLoginButton = findViewById(R.id.readerSdk_login_button);
    readerSdkLogoutButton = findViewById(R.id.readerSdk_logout_button);
    readerSdkLoginStateView = findViewById(R.id.readerSdk_login_state);
    readerSdkLoginButton.setOnClickListener(v -> goToAuthorizeActivity());
    readerSdkLogoutButton.setOnClickListener(v -> deauthorize());
    findViewById(R.id.readerSdk_connect_reader_button).setOnClickListener(v -> connectReader());
  }

  @Override protected void onResume() {
    super.onResume();
    waitingForActivityStart = false;
    updateLoginState();
  }

  @Override protected void onPause() {
    super.onPause();
    settingsStore.setTweetMessage(tweetMessageView.getText().toString());
    try {
      int newAmount = Integer.parseInt(paymentAmountEdittext.getText().toString());
      settingsStore.setPaymentsAmount(newAmount);
    } catch (NumberFormatException ignored) {
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    deauthorizeCallbackRef.clear();
    readerSettingsCallbackRef.clear();
  }

  private void snack(String message) {
    Snackbar.make(twitterLoginButton, message, Snackbar.LENGTH_SHORT).show();
  }

  private void updateTwitterState() {
    TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
    if (session == null) {
      twitterStateView.setText("Twitter: not logged in.");
      twitterLoginButton.setVisibility(View.VISIBLE);
      twitterLogoutButton.setVisibility(View.GONE);
    } else {
      twitterStateView.setText("Twitter: logged in as " + session.getUserName());
      twitterLoginButton.setVisibility(View.GONE);
      twitterLogoutButton.setVisibility(View.VISIBLE);
    }
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    twitterLoginButton.onActivityResult(requestCode, resultCode, data);
  }

  private void connectReader() {
    if (waitingForActivityStart) {
      return;
    }
    waitingForActivityStart = true;
    ReaderManager readerManager = ReaderSdk.readerManager();
    readerManager.startReaderSettingsActivity(this);
  }

  private void goToAuthorizeActivity() {
    if (waitingForActivityStart) {
      return;
    }
    waitingForActivityStart = true;
    Intent intent = new Intent(this, StartAuthorizeActivity.class);
    startActivity(intent);
  }

  private void deauthorize() {
    AuthorizationManager authorizationManager = ReaderSdk.authorizationManager();
    if (authorizationManager.getAuthorizationState().canDeauthorize()) {
      authorizationManager.deauthorize();
    } else {
      showDialog(getString(R.string.cannot_deauthorize_dialog_title),
          getString(R.string.cannot_deauthorize_dialog_message));
    }
  }

  private void updateLoginState() {
    AuthorizationState authorizationState = ReaderSdk.authorizationManager()
        .getAuthorizationState();
    boolean loggedIn = authorizationState.isAuthorized();
    readerSdkLoginButton.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
    readerSdkLogoutButton.setVisibility(loggedIn ? View.VISIBLE : View.GONE);

    if (!loggedIn) {
      readerSdkLoginStateView.setText("Not logged in.");
      return;
    }

    String businessLocation = authorizationState.getAuthorizedLocation().getBusinessName();
    readerSdkLoginStateView.setText(Html.fromHtml(
        String.format("<b><font color='#2780c4'>Merchant Name</color></b><br>%1$s<br><br><b>",
            businessLocation)));
  }

  private void onDeauthorizeResult(
      com.squareup.sdk.reader.core.Result<Void, ResultError<DeauthorizeErrorCode>> result) {
    if (result.isSuccess()) {
      showDialog("Success", "Sucessfully deauthorized");
      updateLoginState();
    } else {
      showErrorDialog(result.getError());
    }
  }

  private void showErrorDialog(ResultError<?> error) {
    String dialogMessage = error.getMessage();
    if (BuildConfig.DEBUG) {
      dialogMessage += "\n\nDebug Message: " + error.getDebugMessage();
      Log.d(TAG, error.getCode() + ": " + error.getDebugCode() + ", " + error.getDebugMessage());
    }
    showDialog(getString(R.string.error_dialog_title), dialogMessage);
  }

  private void showDialog(String title, String message) {
    new AlertDialog.Builder(this).setTitle(title)
        .setMessage(message)
        .setPositiveButton("Ok", null)
        .show();
  }

  private void onReaderSettingsResult(
      com.squareup.sdk.reader.core.Result<Void, ResultError<ReaderSettingsErrorCode>> result) {
    if (result.isError()) {
      ResultError<ReaderSettingsErrorCode> error = result.getError();
      switch (error.getCode()) {
        case SDK_NOT_AUTHORIZED:
          showDialog("Error", "Device not authorized");
          break;
        case USAGE_ERROR:
          showErrorDialog(error);
          break;
      }
    }
  }
}
