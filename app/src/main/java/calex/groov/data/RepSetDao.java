package calex.groov.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.Date;
import java.util.List;

@Dao
public interface RepSetDao {
  @Insert
  long insert(RepSet repSet);

  @Query("SELECT * FROM repset WHERE date BETWEEN :start and :end")
  LiveData<List<RepSet>> range(Date start, Date end);
}
