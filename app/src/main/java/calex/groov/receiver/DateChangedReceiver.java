package calex.groov.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;

import calex.groov.app.GroovApplication;
import calex.groov.model.GroovRepository;

public class DateChangedReceiver extends BroadcastReceiver {

  @Inject GroovRepository repository;

  @Override
  public void onReceive(Context context, Intent intent) {
    ((GroovApplication) context.getApplicationContext()).getComponent().inject(this);
    repository.onDateChanged();
  }
}
