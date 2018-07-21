package calex.groov.worker;

import android.support.annotation.NonNull;

import java.time.Clock;
import java.util.Date;
import java.util.Optional;

import javax.inject.Inject;

import androidx.work.Data;
import androidx.work.Worker;
import calex.groov.app.GroovApplication;
import calex.groov.constant.Constants;
import calex.groov.constant.Keys;
import calex.groov.data.GroovDatabase;
import calex.groov.data.GroovTypeConverters;
import calex.groov.data.RepSet;
import calex.groov.util.GroovUtil;

public class RecordSetWorker extends Worker {

  @Inject GroovDatabase database;
  @Inject Clock clock;

  @NonNull
  @Override
  public Result doWork() {
    ((GroovApplication) getApplicationContext()).getComponent().inject(this);
    int reps = getInputData().getInt(Keys.REPS, -1);
    if (reps == -1) {
      Optional<RepSet> setOptional = database.sets().blockingMostRecent();
      if (setOptional.isPresent()) {
        reps = setOptional.get().getReps();
      } else {
        reps = Constants.DEFAULT_REPS;
      }
    }
    long timestamp = getInputData().getLong(Keys.TIMESTAMP, -1);

    RepSet set = new RepSet();
    set.setDate(
        timestamp != -1 ? GroovTypeConverters.fromTimestamp(timestamp) : new Date(clock.millis()));
    set.setReps(reps);
    database.sets().insert(set);

    setOutputData(new Data.Builder()
        .putInt(Keys.REPS, set.getReps())
        .putInt(
            Keys.COUNT,
            database.sets().blockingTotalReps(
                GroovUtil.todayStartTimestamp(), GroovUtil.todayEndTimestamp()))
        .build());

    return Result.SUCCESS;
  }
}
