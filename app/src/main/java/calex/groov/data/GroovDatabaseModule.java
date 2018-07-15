package calex.groov.data;

import android.arch.persistence.room.Room;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class GroovDatabaseModule {

  private static final String FILE_NAME = "groov-db";

  @Provides
  @Singleton
  public GroovDatabase provideDatabase(Context context) {
    return Room.databaseBuilder(context, GroovDatabase.class, FILE_NAME)
        .fallbackToDestructiveMigration()
        .build();
  }
}
