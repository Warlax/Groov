package calex.groov.worker;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import androidx.work.Worker;
import calex.groov.app.GroovApplication;
import calex.groov.model.GroovRepository;

public class DeleteMostRecentSetWorker extends Worker {

  @Inject GroovRepository repository;

  @NonNull
  @Override
  public Result doWork() {
    ((GroovApplication) getApplicationContext()).getComponent().inject(this);
    repository.blockingDeleteMostRecent();
    return Result.SUCCESS;
  }
}
