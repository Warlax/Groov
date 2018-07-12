package calex.groov.worker;

import android.support.annotation.NonNull;

import java.time.Clock;
import java.util.Date;

import javax.inject.Inject;

import androidx.work.Worker;
import calex.groov.app.GroovApplication;
import calex.groov.constant.Keys;
import calex.groov.data.GroovDatabase;
import calex.groov.data.RepSet;

public class RecordSetWorker extends Worker {

  @Inject GroovDatabase database;
  @Inject Clock clock;

  @NonNull
  @Override
  public Result doWork() {
    ((GroovApplication) getApplicationContext()).getComponent().inject(this);
    int reps = getInputData().getInt(Keys.REPS, -1);
    if (reps == -1) {
      throw new IllegalArgumentException();
    }

    RepSet set = new RepSet();
    set.setDate(new Date(clock.millis()));
    set.setReps(reps);
    database.sets().insert(set);

    return Result.SUCCESS;
  }
}
