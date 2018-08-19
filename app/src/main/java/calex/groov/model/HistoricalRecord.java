package calex.groov.model;

import com.google.auto.value.AutoValue;

import java.util.Date;

@AutoValue
public abstract class HistoricalRecord {
  public abstract Date date();
  public abstract int reps();
  public abstract Builder toBuilder();

  static Builder builder() {
    return new AutoValue_HistoricalRecord.Builder().setReps(0);
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setDate(Date date);
    public abstract Builder setReps(int reps);
    public abstract HistoricalRecord build();
  }
}
