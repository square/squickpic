package com.squareup.photobooth.oauth;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

public final class GoogleOAuthStore {

  private static final String KEY_ACCOUNT_NAME = "accountName";
  private static final String KEY_OAUTH_TOKEN = "token";

  private final SharedPreferences preferences;
  private final AccountManager accountManager;

  public GoogleOAuthStore(Context context) {
    Context appContext = context.getApplicationContext();
    preferences = appContext.getSharedPreferences("google_oauth", Context.MODE_PRIVATE);
    accountManager = AccountManager.get(appContext);
  }

  public void setOAuthToken(String oAuthToken) {
    preferences.edit().putString(KEY_OAUTH_TOKEN, oAuthToken).apply();
  }

  public @Nullable String getOAuthToken() {
    return preferences.getString(KEY_OAUTH_TOKEN, null);
  }

  public void setAccountName(String accountName) {
    preferences.edit().putString(KEY_ACCOUNT_NAME, accountName).apply();
  }

  public @Nullable String getAccountName() {
    return preferences.getString(KEY_ACCOUNT_NAME, null);
  }

  public void invalidateOAuthToken() {
    String token = preferences.getString(KEY_OAUTH_TOKEN, null);
    preferences.edit().remove(KEY_OAUTH_TOKEN).apply();
    accountManager.invalidateAuthToken("com.google", token);
  }
}
