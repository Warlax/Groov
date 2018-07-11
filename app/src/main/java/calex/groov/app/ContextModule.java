package calex.groov.app;

import android.content.Context;

import com.google.common.base.Preconditions;

import dagger.Module;
import dagger.Provides;

@Module
public class ContextModule {
  private final Context context;

  public ContextModule(Context context) {
    this.context = Preconditions.checkNotNull(context);
  }

  @Provides
  public Context provideContext() {
    return context;
  }
}
