package com.squareup.photobooth.job;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.scheduling.GcmJobSchedulerService;
import com.squareup.photobooth.App;

public class GcmJobService extends GcmJobSchedulerService {
  @Override protected JobManager getJobManager() {
    return App.from(this).jobManager();
  }
}