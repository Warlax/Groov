package calex.groov.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

@Database(
    version = 1,
    entities = {
        RepSet.class,
    },
    exportSchema = false)
@TypeConverters({GroovTypeConverters.class})
public abstract class GroovDatabase extends RoomDatabase {
  public abstract RepSetDao sets();
}
