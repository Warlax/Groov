package calex.groov.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.Optional;

import javax.inject.Inject;

import calex.groov.app.GroovApplication;
import calex.groov.data.RepSet;

public class GroovViewModel extends AndroidViewModel {

  @Inject GroovRepository repository;

  public GroovViewModel(@NonNull Application application) {
    super(application);
    ((GroovApplication) application).getComponent().inject(this);
  }

  public LiveData<Integer> repsToday() {
    return repository.repsTodayAsLiveData();
  }

  public LiveData<Optional<RepSet>> mostRecentSet() {
    return repository.mostRecentSetAsLiveData();
  }

  public LiveData<Optional<Boolean>> remind() {
    return repository.remind();
  }

  public void recordSet(int reps) {
    repository.recordCustomSet(reps);
  }

  public void setRemind(boolean remind) {
    repository.setRemind(remind);
  }
}
