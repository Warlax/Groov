package calex.groov.model;

import com.google.auto.value.AutoValue;

import calex.groov.constant.Constants;

@AutoValue
public abstract class RemindSetting {
  public abstract boolean enabled();
  public abstract int intervalMins();

  static Builder builder() {
    return new AutoValue_RemindSetting.Builder()
        .setEnabled(false)
        .setIntervalMins(Constants.DEFAULT_REST_MINS);
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setEnabled(boolean enabled);
    public abstract Builder setIntervalMins(int intervalMins);
    public abstract RemindSetting build();
  }
}
