package calex.groov.app;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class SharedPreferencesModule {

  private static final String NAME = "groov-prefs";

  @Provides
  @Singleton
  public SharedPreferences provideSharedPreferences(Context context) {
    return context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
  }
}
