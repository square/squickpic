package com.squareup.photobooth.twitter;

import android.support.annotation.Nullable;
import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import timber.log.Timber;

public class SendTweetJob extends Job {
  private static final int PRIORITY = 2;

  private final String message;
  private final String mediaId;

  public SendTweetJob(String message, String mediaId) {
    super(new Params(PRIORITY).requireNetwork().persist());
    this.message = message;
    this.mediaId = mediaId;
  }

  @Override public void onAdded() {
  }

  @Override protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
  }

  @Override public void onRun() throws Throwable {
    Tweeter tweeter = new Tweeter();
    tweeter.tweet(message, mediaId);
  }

  @Override protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount,
      int maxRunCount) {
    Timber.d("Could not send tweet", throwable);
    RetryConstraint constraint = new RetryConstraint(true);
    constraint.setNewDelayInMs(1000L);
    return constraint;
  }
}