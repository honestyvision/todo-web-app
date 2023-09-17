package ch.cern.todo.utils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {
  public static Timestamp getSqlTimeStamp(String dateTime) {
    LocalDateTime localDateTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    return Timestamp.valueOf(localDateTime);
  }
}
