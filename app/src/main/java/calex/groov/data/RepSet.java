package calex.groov.data;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverter;

import java.util.Date;

@Entity
public class RepSet {
  @PrimaryKey(autoGenerate = true)
  private final long id;
  private Date date;
  private int reps;

  public RepSet(long id) {
    this.id = id;
  }

  public long getId() {
    return id;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public int getReps() {
    return reps;
  }

  public void setReps(int reps) {
    this.reps = reps;
  }
}
