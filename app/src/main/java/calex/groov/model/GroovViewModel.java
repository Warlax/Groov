package calex.groov.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
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
    MediatorLiveData<Integer> mediator = new MediatorLiveData<>();
    mediator.addSource(repository.repsToday(), reps -> mediator.setValue(reps != null ? reps : 0));
    return mediator;
  }

  public LiveData<Optional<RepSet>> mostRecentSet() {
    return repository.mostRecentSet();
  }

  public LiveData<Optional<Boolean>> remind() {
    return repository.remind();
  }

  public void recordSet(int reps) {
    repository.recordSet(reps);
  }

  public void setRemind(boolean remind) {
    repository.setRemind(remind);
  }
}
