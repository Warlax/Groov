package calex.groov.util;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

public class GroovUtil {
  public static Date todayStartTimestamp() {
    return Date.from(
        LocalDateTime
            .ofInstant(
                new Date().toInstant(), ZoneId.systemDefault())
            .with(LocalTime.MIN)
            .atZone(ZoneId
                .systemDefault())
            .toInstant());
  }

  public static Date todayEndTimestamp() {
    return Date.from(
        LocalDateTime
            .ofInstant(
                new Date().toInstant(), ZoneId.systemDefault())
            .with(LocalTime.MAX)
            .atZone(ZoneId
                .systemDefault())
            .toInstant());
  }

  private GroovUtil() {}
}
