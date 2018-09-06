package com.squareup.photobooth.job;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.scheduling.FrameworkJobSchedulerService;
import com.squareup.photobooth.App;

public class JobService extends FrameworkJobSchedulerService {
  @Override protected JobManager getJobManager() {
    return App.from(this).jobManager();
  }
}