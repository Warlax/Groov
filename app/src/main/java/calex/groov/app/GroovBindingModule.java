package calex.groov.app;

import java.time.Clock;

import calex.groov.data.GroovClock;
import dagger.Binds;
import dagger.Module;

@Module
public interface GroovBindingModule {
  @Binds
  Clock bindClock(GroovClock clock);
}
