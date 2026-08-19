package com.hei.app.service;

import com.hei.app.model.Semester;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;

final class SemesterPeriods {
  private SemesterPeriods() {}

  record Period(Instant start, Instant end) {}

  static Period of(Semester semester, Integer academicYear) {
    boolean firstHalf = semester.ordinal() % 2 == 0;
    Month startMonth = firstHalf ? Month.JANUARY : Month.JULY;
    Month endMonth = firstHalf ? Month.JUNE : Month.DECEMBER;
    int endDay = firstHalf ? 30 : 31;

    Instant start = LocalDateTime.of(academicYear, startMonth, 1, 0, 0).toInstant(ZoneOffset.UTC);
    Instant end =
        LocalDateTime.of(academicYear, endMonth, endDay, 23, 59, 59).toInstant(ZoneOffset.UTC);

    return new Period(start, end);
  }

  static boolean overlaps(Instant historyStart, Instant historyEnd, Period period) {
    return !historyStart.isAfter(period.end())
        && (historyEnd == null || !historyEnd.isBefore(period.start()));
  }
}
