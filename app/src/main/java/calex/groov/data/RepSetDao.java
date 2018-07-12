package calex.groov.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Dao
public interface RepSetDao {
  @Insert
  long insert(RepSet repSet);

  @Query("SELECT * FROM repset ORDER BY date DESC LIMIT 1")
  LiveData<Optional<RepSet>> mostRecent();

  @Query("SELECT SUM(reps) FROM repset WHERE date BETWEEN :start and :end")
  LiveData<Integer> totalReps(Date start, Date end);
}
