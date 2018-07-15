package calex.groov.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.service.quicksettings.TileService;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.WorkStatus;
import calex.groov.R;
import calex.groov.constant.Keys;
import calex.groov.data.GroovDatabase;
import calex.groov.data.RepSet;
import calex.groov.receiver.GroovAppWidgetProvider;
import calex.groov.service.GroovTileService;
import calex.groov.util.GroovUtil;
import calex.groov.worker.RecordSetWorker;

@Singleton
public class GroovRepository {

  private final Context context;
  private final GroovDatabase database;
  private final SharedPreferences preferences;
  private final MutableLiveData<Optional<Boolean>> remind;
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
    remind = new MutableLiveData<>();
    remind.setValue(Optional.empty());
    preferences.registerOnSharedPreferenceChangeListener(
        (sharedPreferences, key) -> {
          if (TextUtils.equals(key, Keys.REMIND)) {
            updateRemindFromPreferences();
          }
        });
  }

  public LiveData<Optional<RepSet>> mostRecentSetAsLiveData() {
    return database.sets().mostRecentAsLiveData();
  }

  public LiveData<Integer> repsTodayAsLiveData() {
    return database.sets().totalRepsAsLiveData(
        GroovUtil.todayStartTimestamp(), GroovUtil.todayEndTimestamp());
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
    recordCustomSet(null);
  }

  public void recordCustomSet(@Nullable Integer reps) {
    Data.Builder inputDataBuilder = new Data.Builder();
    if (reps != null) {
      inputDataBuilder.putInt(Keys.REPS, reps);
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

          Toast.makeText(
              context,
              context.getResources().getString(R.string.reps_added, repsRecorded, repsToday),
              Toast.LENGTH_SHORT)
              .show();

          GroovAppWidgetProvider.sendUpdate(context, repsRecorded, repsToday);

          TileService.requestListeningState(
              context, new ComponentName(context, GroovTileService.class));
        }
      }
    };
    workManager.getStatusById(workRequest.getId()).observeForever(observer);
  }

  public void setRemind(boolean remind) {
    preferences.edit().putBoolean(Keys.REMIND, remind).apply();;
  }

  public LiveData<Optional<Boolean>> remind() {
    return remind;
  }

  public void insertSets(List<RepSet> sets) {
    database.sets().insert(sets);
  }

  private void updateRemindFromPreferences() {
    remind.setValue(Optional.of(preferences.getBoolean(Keys.REMIND, false)));
  }
}
