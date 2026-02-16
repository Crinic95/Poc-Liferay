package it.dedagroup.contratti.microservice.util;

import java.time.LocalDate;

public class OracleDateUtil {

    private OracleDateUtil() {}

    public static LocalDate yyyymmddToLocalDate(Long yyyymmdd) {
        if (yyyymmdd == null || yyyymmdd == 0) return null;

        String s = String.valueOf(yyyymmdd);
        if (s.length() != 8) return null;

        int year = Integer.parseInt(s.substring(0, 4));
        int month = Integer.parseInt(s.substring(4, 6));
        int day = Integer.parseInt(s.substring(6, 8));

        return LocalDate.of(year, month, day);
    }
}