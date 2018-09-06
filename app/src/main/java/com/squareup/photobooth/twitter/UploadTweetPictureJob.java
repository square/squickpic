package com.squareup.photobooth.twitter;

import android.support.annotation.Nullable;
import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.squareup.photobooth.App;
import com.squareup.photobooth.job.InjectedJob;
import timber.log.Timber;

public class UploadTweetPictureJob extends InjectedJob {
  private static final int PRIORITY = 2;

  private final byte[] jpegBytes;
  private final String message;
  private transient volatile JobManager jobManager;

  public UploadTweetPictureJob(byte[] jpegBytes, String message) {
    super(new Params(PRIORITY).requireNetwork().persist());
    this.jpegBytes = jpegBytes;
    this.message = message;
  }

  @Override public void onAdded() {
  }

  @Override protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
  }

  @Override public void onRun() throws Throwable {
    Tweeter tweeter = new Tweeter();
    String mediaId = tweeter.uploadPicture(jpegBytes);
    jobManager.addJob(new SendTweetJob(message, mediaId));
  }

  @Override protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount,
      int maxRunCount) {
    Timber.d("Could not upload tweet picture", throwable);
    RetryConstraint constraint = new RetryConstraint(true);
    constraint.setNewDelayInMs(1000L);
    return constraint;
  }

  @Override public void inject(App app) {
    jobManager = app.jobManager();
  }
}