package calex.groov.app;

import androidx.work.WorkManager;
import dagger.Module;
import dagger.Provides;

@Module
public class WorkManagerModule {
  @Provides
  public WorkManager provideWorkManager() {
    return WorkManager.getInstance();
  }
}
