package com.squareup.photobooth.printer;

import android.support.annotation.Nullable;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.squareup.photobooth.App;
import com.squareup.photobooth.job.InjectedJob;
import timber.log.Timber;

public class PrintJob extends InjectedJob {
  private static final int PRIORITY = 1;

  private final byte[] jpegBytes;
  private transient volatile CloudPrinter cloudPrinter;

  public PrintJob(byte[] jpegBytes) {
    super(new Params(PRIORITY).requireNetwork().persist());
    this.jpegBytes = jpegBytes;
  }

  @Override public void onAdded() {
  }

  @Override protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
  }

  @Override public void onRun() throws Throwable {
    cloudPrinter.cloudPrint(jpegBytes);
  }

  @Override protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount,
      int maxRunCount) {
    Timber.d("Could not submit picture", throwable);
    RetryConstraint constraint = new RetryConstraint(true);
    constraint.setNewDelayInMs(1000L);
    return constraint;
  }

  @Override public void inject(App app) {
    cloudPrinter = app.cloudPrinter();
  }
}