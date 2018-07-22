package calex.groov.data;

import javax.inject.Inject;

import dagger.Reusable;

@Reusable
public class GroovClock implements Clock {

  @Inject
  public GroovClock() {}

  @Override
  public long millis() {
    return System.currentTimeMillis();
  }
}
