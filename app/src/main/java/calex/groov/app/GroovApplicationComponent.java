package calex.groov.app;

import javax.inject.Singleton;

import calex.groov.activity.MainActivity;
import calex.groov.data.GroovDatabaseModule;
import dagger.Component;

@Singleton
@Component(modules = {
    ContextModule.class,
    GroovDatabaseModule.class,
})
@GroovApplicationScope
public interface GroovApplicationComponent {
  void inject(MainActivity activity);
}

