package com.db.accountsystem.utils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {

    public static final String DATE_FORMAT_yyyy_MM_dd_T_HHmmss_SSS_Z = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static Timestamp generateCurrentTimestamp() {
        return Timestamp.valueOf(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
    }

    public static String fromDate(LocalDateTime localDateTime, String dateformat) {
        return localDateTime.format(DateTimeFormatter.ofPattern(dateformat));
    }

}
