package calex.groov.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import java.util.Optional;

import javax.inject.Inject;

import calex.groov.app.GroovApplication;
import calex.groov.constant.Constants;
import calex.groov.data.RepSet;
import calex.groov.model.GroovRepository;
import calex.groov.receiver.GroovAppWidgetProvider;

public class UpdateAppWidgetService extends IntentService {

  private static final String NAME = "UpdateAppWidgetService";

  public static Intent newIntent(Context context) {
    return new Intent(context, UpdateAppWidgetService.class);
  }

  @Inject GroovRepository repository;

  public UpdateAppWidgetService() {
    super(NAME);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    ((GroovApplication) getApplication()).getComponent().inject(this);
  }

  @Override
  protected void onHandleIntent(@Nullable Intent intent) {
    Optional<RepSet> mostRecentSet = repository.blockingMostRecentSet();
    int repsToRecord =
        mostRecentSet.isPresent() ? mostRecentSet.get().getReps() : Constants.DEFAULT_REPS;
    Integer repTotalToday = repository.blockingRepsToday();
    GroovAppWidgetProvider.sendUpdate(
        this, repsToRecord, repTotalToday != null ? repTotalToday : 0);
  }
}
