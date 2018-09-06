package com.squareup.photobooth.result;

import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.support.annotation.Nullable;
import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.squareup.photobooth.App;
import com.squareup.photobooth.printer.PrintJob;
import com.squareup.photobooth.twitter.Tweeter;
import com.squareup.photobooth.twitter.UploadTweetPictureJob;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import timber.log.Timber;

/** This job is not serialized. */
public class SavePictureToDiskJob extends Job {

  private final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);

  private static final int PRIORITY = 1;

  private final JobManager jobManager;
  private final Bitmap bitmap;
  private final App app;
  private final String message;
  private final boolean cloudPrint;
  private final boolean tweet;

  public SavePictureToDiskJob(App app, Bitmap bitmap, String message, boolean cloudPrint, boolean tweet) {
    super(new Params(PRIORITY));
    this.app = app;
    jobManager = app.jobManager();
    this.bitmap = bitmap;
    this.message = message;
    this.cloudPrint = cloudPrint;
    this.tweet = tweet;
  }

  @Override public void onAdded() {
  }

  @Override protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
  }

  @Override public void onRun() throws Throwable {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
    byte[] jpegBytes = stream.toByteArray();
    if (tweet && new Tweeter().isLoggedIn()) {
      jobManager.addJob(new UploadTweetPictureJob(jpegBytes, message));
    }

    if (cloudPrint) {
      jobManager.addJob(new PrintJob(jpegBytes));
    }

    saveToDevice(bitmap);
  }

  private void saveToDevice(Bitmap bitmap) throws IOException {
    File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    String timeStamp = timeFormat.format(new Date());
    String imageFileName = "squickpic_" + timeStamp + ".jpeg";
    File file = new File(path, imageFileName);

    path.mkdirs();

    OutputStream outputStream = new FileOutputStream(file);
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
    outputStream.close();

    // Tell the media scanner about the new file so that it is immediately available to the user.
    MediaScannerConnection.scanFile(app, new String[] { file.toString() }, null,
        (path1, uri) -> Timber.d("Filed added %s", uri));
  }

  @Override protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount,
      int maxRunCount) {
    Timber.d("Could not save picture", throwable);
    return RetryConstraint.CANCEL;
  }
}