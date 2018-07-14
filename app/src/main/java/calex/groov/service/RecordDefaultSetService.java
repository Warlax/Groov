package calex.groov.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import calex.groov.app.GroovApplication;
import calex.groov.model.GroovRepository;

public class RecordDefaultSetService extends IntentService {

  private static final String SERVICE_NAME = "RecordDefaultSetService";

  public static Intent newIntent(Context context) {
    return new Intent(context, RecordDefaultSetService.class);
  }

  @Inject GroovRepository repository;

  public RecordDefaultSetService() {
    super(SERVICE_NAME);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    ((GroovApplication) getApplication()).getComponent().inject(this);
  }

  @Override
  protected void onHandleIntent(@Nullable Intent intent) {
    repository.recordDefaultSet();
  }
}
