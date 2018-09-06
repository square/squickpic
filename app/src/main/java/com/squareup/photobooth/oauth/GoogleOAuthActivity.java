package com.squareup.photobooth.oauth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;
import com.squareup.photobooth.App;

public class GoogleOAuthActivity extends Activity {

  private static final String CLOUD_PRINT_SCOPE =
      "oauth2:https://www.googleapis.com/auth/cloudprint";

  private static final int ACCOUNT_CODE = 1;
  private static final int CREDENTIALS_CODE = 2;

  private AccountManager accountManager;
  private GoogleOAuthStore oAuthStore;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    accountManager = AccountManager.get(this);
    oAuthStore = App.from(this).oAuthStore();

    if (savedInstanceState == null) {
      Intent intent =
          AccountManager.newChooseAccountIntent(null, null, new String[] { "com.google" }, false,
              null, null, null, null);
      startActivityForResult(intent, ACCOUNT_CODE);
    }
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (resultCode == RESULT_OK) {
      if (requestCode == CREDENTIALS_CODE) {
        requestToken();
      } else if (requestCode == ACCOUNT_CODE) {
        String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        oAuthStore.setAccountName(accountName);
        oAuthStore.invalidateOAuthToken();

        requestToken();
      }
    } else {
      String message;
      if (requestCode == CREDENTIALS_CODE) {
        message = "Failed to obtain credentials";
      } else if (requestCode == ACCOUNT_CODE) {
        message = "Failed to select credentials";
      } else {
        message =
            "Unexpected result code " + resultCode + " for unknown request code " + requestCode;
      }
      finishWithToast(message);
    }
  }

  private void requestToken() {
    String accountName = oAuthStore.getAccountName();
    Account userAccount = null;
    for (Account account : accountManager.getAccountsByType("com.google")) {
      if (account.name.equals(accountName)) {
        userAccount = account;
        break;
      }
    }
    if (userAccount == null) {
      finishWithToast("Could not find account for " + accountName);
      return;
    }

    accountManager.getAuthToken(userAccount,
        CLOUD_PRINT_SCOPE, null, this, future -> {
          Bundle bundle;
          try {
            bundle = future.getResult();
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
          Intent credentialsIntent = (Intent) bundle.get(AccountManager.KEY_INTENT);
          if (credentialsIntent != null) {
            startActivityForResult(credentialsIntent, CREDENTIALS_CODE);
          } else {
            String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
            oAuthStore.setOAuthToken(token);
            finishWithToast("OAuth Successful");
          }
        }, null);
  }

  private void finishWithToast(String message) {
    Toast.makeText(GoogleOAuthActivity.this, message, Toast.LENGTH_LONG).show();
    finish();
  }
}
