package calex.groov.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import calex.groov.constant.Keys;
import calex.groov.data.GroovDatabase;
import calex.groov.data.RepSet;
import calex.groov.worker.RecordSetWorker;

@Singleton
public class GroovRepository {

  private final GroovDatabase database;
  private final SharedPreferences preferences;
  private final MutableLiveData<Optional<Boolean>> remind;
  private final WorkManager workManager;

  @Inject
  public GroovRepository(
      GroovDatabase database,
      SharedPreferences preferences,
      WorkManager workManager) {
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

  public LiveData<Optional<RepSet>> mostRecentSet() {
    return database.sets().mostRecent();
  }

  public LiveData<Integer> repsToday() {
    return database.sets().totalReps(todayStartTimestamp(), todayEndTimestamp());
  }

  public void recordSet(int reps) {
    workManager.enqueue(new OneTimeWorkRequest.Builder(RecordSetWorker.class)
        .setInputData(new Data.Builder()
            .putInt(Keys.REPS, reps)
            .build())
        .build());
  }

  public void setRemind(boolean remind) {
    preferences.edit().putBoolean(Keys.REMIND, remind).apply();;
  }

  public LiveData<Optional<Boolean>> remind() {
    return remind;
  }

  private void updateRemindFromPreferences() {
    remind.setValue(Optional.of(preferences.getBoolean(Keys.REMIND, false)));
  }

  private static Date todayStartTimestamp() {
    return Date.from(
        LocalDateTime
            .ofInstant(
                new Date().toInstant(), ZoneId.systemDefault())
            .with(LocalTime.MIN)
            .atZone(ZoneId
                .systemDefault())
            .toInstant());
  }

  private static Date todayEndTimestamp() {
    return Date.from(
        LocalDateTime
            .ofInstant(
                new Date().toInstant(), ZoneId.systemDefault())
            .with(LocalTime.MAX)
            .atZone(ZoneId
                .systemDefault())
            .toInstant());
  }
}
