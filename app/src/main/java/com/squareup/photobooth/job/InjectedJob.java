package com.squareup.photobooth.job;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.squareup.photobooth.App;

public abstract class InjectedJob extends Job {

  protected InjectedJob(Params params) {
    super(params);
  }

  public abstract void inject(App app);
}
