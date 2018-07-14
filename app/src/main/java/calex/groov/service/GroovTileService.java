package calex.groov.service;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import calex.groov.R;
import calex.groov.app.GroovApplication;
import calex.groov.model.GroovRepository;

public class GroovTileService extends TileService implements Observer<Integer> {

  @Inject GroovRepository repository;
  private LiveData<Integer> repsToday;

  @Override
  public void onCreate() {
    super.onCreate();
    ((GroovApplication) getApplication()).getComponent().inject(this);
    repsToday = repository.repsTodayAsLiveData();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    repsToday = null;
  }

  @Override
  public void onStartListening() {
    repsToday.observeForever(this);
  }

  @Override
  public void onStopListening() {
    repsToday.removeObserver(this);
  }

  @Override
  public void onClick() {
    repository.recordDefaultSet();
  }

  @Override
  public void onChanged(@Nullable Integer repCount) {
    if (repCount == null) {
      return;
    }

    Tile tile = getQsTile();
    tile.setState(Tile.STATE_ACTIVE);
    tile.setLabel(getString(R.string.reps_today, repCount));
    tile.updateTile();
  }
}
