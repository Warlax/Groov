package calex.groov.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.support.annotation.NonNull;
import android.util.LongSparseArray;

import org.apache.commons.lang3.time.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import calex.groov.app.GroovApplication;
import calex.groov.data.RepSet;

public class HistoryViewModel extends AndroidViewModel {

  @Inject GroovRepository repository;
  private final MediatorLiveData<List<HistoricalRecord>> historicalRecords;

  public HistoryViewModel(@NonNull Application application) {
    super(application);
    ((GroovApplication) application).getComponent().inject(this);
    historicalRecords = new MediatorLiveData<>();
    historicalRecords.addSource(
        repository.allSetsAsLiveData(),
        allSets -> {
          if (allSets == null) {
            historicalRecords.setValue(Collections.emptyList());
            return;
          }

          LongSparseArray<HistoricalRecord> recordByDate = new LongSparseArray<>();
          for (RepSet set : allSets) {
            Date date = DateUtils.truncate(set.getDate(), Calendar.DAY_OF_MONTH);
            HistoricalRecord record = recordByDate.get(date.getTime());
            if (record == null) {
              record = HistoricalRecord.builder().setDate(date).build();
            }
            recordByDate.put(
                date.getTime(),
                record.toBuilder().setReps(record.reps() + set.getReps()).build());
          }
          List<HistoricalRecord> records = new ArrayList<>(recordByDate.size());
          for (int i = 0; i < recordByDate.size(); i++) {
            records.add(recordByDate.valueAt(i));
          }
          Collections.sort(records, (r1, r2) -> r2.date().compareTo(r1.date()));
          historicalRecords.setValue(records);
        });
  }

  public LiveData<List<HistoricalRecord>> historicalRecords() {
    return historicalRecords;
  }
}
