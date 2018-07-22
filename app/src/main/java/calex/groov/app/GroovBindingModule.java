package calex.groov.app;

import calex.groov.data.Clock;
import calex.groov.data.GroovClock;
import dagger.Binds;
import dagger.Module;

@Module
public interface GroovBindingModule {
  @Binds
  Clock bindClock(GroovClock clock);
}
