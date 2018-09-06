package com.squareup.photobooth.settings;

import android.content.Context;
import android.content.SharedPreferences;

public final class SettingsStore {
  public static final int DEFAULT_AMOUNT = 1_00;

  private static final String LOCK_TASK_KEY = "lockTask";
  private static final String PAYMENTS_ENABLED_KEY = "paymentsEnabled";
  private static final String AMOUNT_KEY = "amount";
  private static final String TWEET_MESSAGE_KEY = "tweetMessage";
  private static final String LOG_HTTP_KEY = "logHttp";
  private final SharedPreferences preferences;

  public SettingsStore(Context context) {
    Context appContext = context.getApplicationContext();
    preferences = appContext.getSharedPreferences("settings", Context.MODE_PRIVATE);
  }

  public void setLockTask(boolean lockTask) {
    preferences.edit().putBoolean(LOCK_TASK_KEY, lockTask).apply();
  }

  public boolean shouldLockTask() {
    return preferences.getBoolean(LOCK_TASK_KEY, true);
  }

  public void setLogHttp(boolean logHttp) {
    preferences.edit().putBoolean(LOG_HTTP_KEY, logHttp).apply();
  }

  public boolean shouldLogHttp() {
    return preferences.getBoolean(LOG_HTTP_KEY, false);
  }

  public void setPaymentsEnabled(boolean paymentsEnabled) {
    preferences.edit().putBoolean(PAYMENTS_ENABLED_KEY, paymentsEnabled).apply();
  }

  public boolean arePaymentsEnabled() {
    return preferences.getBoolean(PAYMENTS_ENABLED_KEY, true);
  }

  public void setPaymentsAmount(int amount) {
    preferences.edit().putInt(AMOUNT_KEY, amount).apply();
  }

  public int getPaymentsAmount() {
    return preferences.getInt(AMOUNT_KEY, DEFAULT_AMOUNT);
  }

  public void setTweetMessage(String message) {
    preferences.edit().putString(TWEET_MESSAGE_KEY, message).apply();
  }

  public String getTweetMessage() {
    return preferences.getString(TWEET_MESSAGE_KEY, "Snap!");
  }
}
