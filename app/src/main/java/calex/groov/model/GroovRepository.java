package calex.groov.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.service.quicksettings.TileService;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.common.base.Optional;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.WorkStatus;
import calex.groov.R;
import calex.groov.constant.Constants;
import calex.groov.constant.Keys;
import calex.groov.data.GroovDatabase;
import calex.groov.data.RepSet;
import calex.groov.receiver.GroovAppWidgetProvider;
import calex.groov.service.GroovTileService;
import calex.groov.util.GroovUtil;
import calex.groov.worker.RecordSetWorker;
import calex.groov.worker.ReminderWorker;
import io.reactivex.Observable;

@Singleton
public class GroovRepository {

  private final Context context;
  private final GroovDatabase database;
  private final SharedPreferences preferences;
  private final ChangeableSourceMediatorLiveData<Integer> repsToday;
  private final ChangeableSourceMediatorLiveData<Optional<RepSet>> mostRecentSet;
  private final MutableLiveData<RemindSetting> remind;
  private final WorkManager workManager;

  @Inject
  public GroovRepository(
      Context context,
      GroovDatabase database,
      SharedPreferences preferences,
      WorkManager workManager) {
    this.context = context;
    this.database = database;
    this.preferences = preferences;
    this.workManager = workManager;
    repsToday = new ChangeableSourceMediatorLiveData<>(0);
    mostRecentSet = new ChangeableSourceMediatorLiveData<>(Optional.absent());
    onDateChanged();
    remind = new MutableLiveData<>();
    remind.setValue(RemindSetting.builder()
        .setEnabled(preferences.getBoolean(Keys.REMIND, false))
        .setIntervalMins(preferences.getInt(Keys.REST_DURATION, Constants.DEFAULT_REST_MINS))
        .build());
  }

  public LiveData<Optional<RepSet>> mostRecentSetAsLiveData() {
    return mostRecentSet;
  }

  public LiveData<Integer> repsTodayAsLiveData() {
    return repsToday;
  }

  public Optional<RepSet> blockingMostRecentSet() {
    return database.sets().blockingMostRecent();
  }

  public Integer blockingRepsToday() {
    return database.sets().blockingTotalReps(
        GroovUtil.todayStartTimestamp(), GroovUtil.todayEndTimestamp());
  }

  public List<RepSet> blockingAllSets() {
    return database.sets().blockingAll();
  }

  public void recordDefaultSet() {
    recordCustomSet(null, null);
  }

  public void recordCustomSet(@Nullable Integer reps, @Nullable Long timestamp) {
    Data.Builder inputDataBuilder = new Data.Builder();
    if (reps != null) {
      inputDataBuilder.putInt(Keys.REPS, reps);
    }
    if (timestamp != null) {
      inputDataBuilder.putLong(Keys.TIMESTAMP, timestamp);
    }
    WorkRequest workRequest = new OneTimeWorkRequest.Builder(RecordSetWorker.class)
        .setInputData(inputDataBuilder.build())
        .build();
    workManager.enqueue(workRequest);
    Observer<WorkStatus> observer = new Observer<WorkStatus>() {
      @Override
      public void onChanged(@Nullable WorkStatus workStatus) {
        if (workStatus.getState().isFinished()) {
          workManager.getStatusById(workStatus.getId()).removeObserver(this);

          Data outputData = workStatus.getOutputData();
          int repsRecorded = outputData.getInt(Keys.REPS, 0);
          int repsToday = outputData.getInt(Keys.COUNT, 0);

          if (timestamp == null) {
            Toast.makeText(
                context,
                context.getResources().getString(R.string.reps_added, repsRecorded, repsToday),
                Toast.LENGTH_SHORT)
                .show();
          }

          GroovAppWidgetProvider.sendUpdate(context, repsRecorded, repsToday);

          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            TileService.requestListeningState(
                context, new ComponentName(context, GroovTileService.class));
          }
        }
      }
    };
    workManager.getStatusById(workRequest.getId()).observeForever(observer);

    if (remind.getValue() != null && remind.getValue().enabled()) {
      ReminderWorker.schedule(workManager, remind.getValue().intervalMins());
    }
  }

  public void setRemind(boolean enabled, int intervalMins) {
    preferences.edit()
        .putBoolean(Keys.REMIND, enabled)
        .putInt(Keys.REST_DURATION, intervalMins)
        .apply();
    remind.setValue(
        RemindSetting.builder()
            .setEnabled(enabled)
            .setIntervalMins(intervalMins)
            .build());

    if (enabled) {
      ReminderWorker.schedule(workManager, intervalMins);
    } else {
      ReminderWorker.cancel(workManager);
    }
  }

  public LiveData<RemindSetting> remindAsLiveData() {
    return remind;
  }

  public RemindSetting remind() {
    return remind.getValue() != null ? remind.getValue() : RemindSetting.builder().build();
  }

  public void insertSets(List<RepSet> sets) {
    database.sets().insert(sets);
  }

  public RepSet blockingDeleteMostRecent() {
    Optional<RepSet> setOptional = blockingMostRecentSet();
    if (!setOptional.isPresent()) {
      return null;
    }

    database.sets().delete(setOptional.get());
    return setOptional.get();
  }

  public void onDateChanged() {
    repsToday.updateSource(database.sets().totalRepsAsLiveData(
        GroovUtil.todayStartTimestamp(), GroovUtil.todayEndTimestamp()));
    mostRecentSet.updateSource(database.sets().mostRecentAsLiveData());
  }

  public LiveData<List<RepSet>> allSetsAsLiveData() {
    return database.sets().allAsLiveData();
  }

  private static class ChangeableSourceMediatorLiveData<T> extends MediatorLiveData<T> {
    private final T defaultValue;
    private LiveData<T> source;

    public ChangeableSourceMediatorLiveData(@Nullable T defaultValue) {
      this.defaultValue = defaultValue;
    }

    public void updateSource(LiveData<T> source) {
      if (this.source != null) {
        removeSource(this.source);
      }
      this.source = source;
      addSource(source, reps -> setValue(reps != null ? reps : defaultValue));
    }
  }
}
