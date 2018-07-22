package calex.groov.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.google.common.base.Optional;

import java.util.Date;
import java.util.List;

@Dao
public interface RepSetDao {
  @Insert
  long insert(RepSet set);

  @Insert
  void insert(List<RepSet> sets);

  @Query("SELECT * FROM repset ORDER BY date DESC LIMIT 1")
  LiveData<Optional<RepSet>> mostRecentAsLiveData();

  @Query("SELECT * FROM repset ORDER BY date DESC LIMIT 1")
  Optional<RepSet> blockingMostRecent();

  @Query("SELECT SUM(reps) FROM repset WHERE date BETWEEN :start and :end")
  LiveData<Integer> totalRepsAsLiveData(Date start, Date end);

  @Query("SELECT SUM(reps) FROM repset WHERE date BETWEEN :start and :end")
  Integer blockingTotalReps(Date start, Date end);

  @Query("SELECT * FROM repset ORDER BY date ASC")
  List<RepSet> blockingAll();

  @Delete
  void delete(RepSet set);
}
