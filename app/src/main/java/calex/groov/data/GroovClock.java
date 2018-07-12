package calex.groov.data;

import com.google.common.base.Preconditions;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import javax.inject.Inject;

import dagger.Reusable;

@Reusable
public class GroovClock extends Clock {

  private final ZoneId zoneId;

  @Inject
  public GroovClock() {
    this(ZoneId.systemDefault());
  }

  private GroovClock(ZoneId zoneId) {
    this.zoneId = Preconditions.checkNotNull(zoneId);
  }

  @Override
  public ZoneId getZone() {
    return zoneId;
  }

  @Override
  public Clock withZone(ZoneId zoneId) {
    return new GroovClock(zoneId);
  }

  @Override
  public Instant instant() {
    return Instant.ofEpochMilli(System.currentTimeMillis());
  }
}
