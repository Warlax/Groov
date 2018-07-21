package calex.groov.worker;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import androidx.work.Data;
import androidx.work.Worker;
import calex.groov.app.GroovApplication;
import calex.groov.constant.Keys;
import calex.groov.data.GroovTypeConverters;
import calex.groov.data.RepSet;
import calex.groov.model.GroovRepository;

public class DeleteMostRecentSetWorker extends Worker {

  @Inject GroovRepository repository;

  @NonNull
  @Override
  public Result doWork() {
    ((GroovApplication) getApplicationContext()).getComponent().inject(this);
    RepSet deleted = repository.blockingDeleteMostRecent();
    if (deleted != null) {
      setOutputData(new Data.Builder()
          .putLong(
              Keys.TIMESTAMP,
              GroovTypeConverters.dateToTimestamp(deleted.getDate()))
          .putInt(
              Keys.REPS,
              deleted.getReps())
          .build());
    }
    return Result.SUCCESS;
  }
}
