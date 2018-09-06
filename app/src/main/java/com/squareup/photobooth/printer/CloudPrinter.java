package com.squareup.photobooth.printer;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Log;
import com.google.gson.Gson;
import com.squareup.photobooth.BuildConfig;
import com.squareup.photobooth.oauth.GoogleOAuthStore;
import com.squareup.photobooth.oauth.OAuthHeaderInterceptor;
import com.squareup.photobooth.settings.SettingsStore;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

import static com.squareup.photobooth.printer.CloudPrintService.CONNECTION_STATUS_ALL;

public class CloudPrinter {

  public static final String PRINT_FILENAME = "squickpic.jpeg";

  private static final String CLOUDPRINT_URL = "https://www.google.com/cloudprint/";
  private static final String KEY_PRINTER_ID = "printerId";
  private static final String KEY_PRINTER_NAME = "printerName";
  private static final String FILE_TITLE = "Squickpic";

  public static CloudPrinter create(GoogleOAuthStore oAuthStore, SettingsStore settingsStore,
      Context context) {
    Interceptor authorizationInterceptor = new OAuthHeaderInterceptor(oAuthStore);
    OkHttpClient.Builder builder = new OkHttpClient.Builder();
    if (BuildConfig.DEBUG && settingsStore.shouldLogHttp()) {
      HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
      loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
      builder.addInterceptor(loggingInterceptor);
    }
    OkHttpClient client = builder.addInterceptor(authorizationInterceptor).build();
    Retrofit retrofit = new Retrofit.Builder().baseUrl(CLOUDPRINT_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build();
    CloudPrintService cloudPrintService = retrofit.create(CloudPrintService.class);
    return new CloudPrinter(cloudPrintService, context);
  }

  private final CloudPrintService cloudPrintService;
  private final SharedPreferences preferences;
  private final Gson gson;

  private CloudPrinter(CloudPrintService cloudPrintService, Context context) {
    this.cloudPrintService = cloudPrintService;
    Context appContext = context.getApplicationContext();
    preferences = appContext.getSharedPreferences("submit", Context.MODE_PRIVATE);
    gson = new Gson();
  }

  public void search(Callback<CloudPrintService.SearchResponse> callback) {
    cloudPrintService.search(CONNECTION_STATUS_ALL, null, true).enqueue(callback);
  }

  public void selectPrinter(CloudPrintService.Printer printer) {
    preferences.edit()
        .putString(KEY_PRINTER_ID, printer.id)
        .putString(KEY_PRINTER_NAME, printer.description)
        .apply();
  }

  @Nullable public String getSelectedPrinterName() {
    return preferences.getString(KEY_PRINTER_NAME, null);
  }

  public void clearPrinter() {
    preferences.edit().remove(KEY_PRINTER_ID).remove(KEY_PRINTER_NAME).apply();
  }

  public void cloudPrint(byte[] jpegBytes) throws IOException {
    String printerId = preferences.getString(KEY_PRINTER_ID, null);
    if (printerId == null) {
      throw new UnsupportedOperationException("Please check isCloudPrintReady() first");
    }

    CloudPrintService.Ticket ticket = new CloudPrintService.Ticket();
    String serializedTicket = gson.toJson(ticket);

    RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), jpegBytes);

    MultipartBody.Part content =
        MultipartBody.Part.createFormData("content", PRINT_FILENAME, requestFile);
    RequestBody printerIdBody = RequestBody.create(MultipartBody.FORM, printerId);
    RequestBody titleBody = RequestBody.create(MultipartBody.FORM, FILE_TITLE);
    RequestBody ticketBody = RequestBody.create(MultipartBody.FORM, serializedTicket);
    RequestBody contentTypeBody = RequestBody.create(MultipartBody.FORM, "image/jpeg");
    Response<CloudPrintService.SubmitResponse> printResponse =
        cloudPrintService.submit(printerIdBody, titleBody, ticketBody, content, contentTypeBody)
            .execute();

    if (!printResponse.isSuccessful()) {
      throw new IOException("Print submit not successful");
    }
    CloudPrintService.SubmitResponse submitResponse = printResponse.body();

    if (!submitResponse.success) {
      throw new IOException("Server error: " + submitResponse.message);
    }
  }

  public boolean isCloudPrintReady() {
    String printerId = preferences.getString(KEY_PRINTER_ID, null);
    return printerId != null;
  }
}
