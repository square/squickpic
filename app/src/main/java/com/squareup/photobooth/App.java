package com.squareup.photobooth;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.util.Log;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;
import com.birbit.android.jobqueue.scheduling.FrameworkJobSchedulerService;
import com.birbit.android.jobqueue.scheduling.GcmJobSchedulerService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.squareup.photobooth.job.GcmJobService;
import com.squareup.photobooth.job.JobManagerInjector;
import com.squareup.photobooth.job.JobService;
import com.squareup.photobooth.job.TimberJobLogger;
import com.squareup.photobooth.oauth.GoogleOAuthStore;
import com.squareup.photobooth.printer.CloudPrinter;
import com.squareup.photobooth.settings.SettingsStore;
import com.squareup.photobooth.snap.BitmapBytesHolder;
import com.squareup.photobooth.snap.PictureRenderer;
import com.squareup.sdk.reader.ReaderSdk;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterConfig;
import timber.log.Timber;

public class App extends Application {

  // Created by py+api@squareup.com on production.
  private static final String CLIENT_ID = "sq0idp-8LyKI0yIn42eM_dg8a4ciw";

  private GoogleOAuthStore oAuthStore;
  private CloudPrinter cloudPrinter;
  private PictureRenderer pictureRenderer;
  private BitmapBytesHolder bitmapBytesHolder;
  private JobManager jobManager;
  private SettingsStore settingsStore;

  public static App from(Context context) {
    return (App) context.getApplicationContext();
  }

  @Override public void onCreate() {
    super.onCreate();
    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree());
    }
    TwitterConfig config =
        new TwitterConfig.Builder(this).logger(new DefaultLogger(Log.DEBUG)).debug(true).build();
    Twitter.initialize(config);
    settingsStore = new SettingsStore(this);
    oAuthStore = new GoogleOAuthStore(this);
    cloudPrinter = CloudPrinter.create(oAuthStore, settingsStore, this);
    pictureRenderer = new PictureRenderer(this);
    bitmapBytesHolder = new BitmapBytesHolder();

    Configuration.Builder builder = new Configuration.Builder(this) //
        .minConsumerCount(1) //
        .maxConsumerCount(3) //
        .loadFactor(3) //
        .customLogger(new TimberJobLogger()).injector(new JobManagerInjector(this)) //
        .consumerKeepAlive(120);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      builder.scheduler(FrameworkJobSchedulerService.createSchedulerFor(this, JobService.class),
          true);
    } else {
      int enableGcm = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
      if (enableGcm == ConnectionResult.SUCCESS) {
        builder.scheduler(GcmJobSchedulerService.createSchedulerFor(this, GcmJobService.class),
            true);
      }
    }
    jobManager = new JobManager(builder.build());
    ReaderSdk.initialize(this);
  }

  public GoogleOAuthStore oAuthStore() {
    return oAuthStore;
  }

  public CloudPrinter cloudPrinter() {
    return cloudPrinter;
  }

  public PictureRenderer pictureRenderer() {
    return pictureRenderer;
  }

  public BitmapBytesHolder bitmapHolder() {
    return bitmapBytesHolder;
  }

  public JobManager jobManager() {
    return jobManager;
  }

  public SettingsStore settingsStore() {
    return settingsStore;
  }

  @Override protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
    // Required if minSdkVersion < 21
    MultiDex.install(this);
  }
}
