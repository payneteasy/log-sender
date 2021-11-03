package com.payneteasy.logsender.application.parser;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static java.lang.Character.isDigit;

public class DateParser {

    public static OffsetDateTime substringLocalDateTime(String line) {
        // 01234567890123456789012
        // yyyy-MM-dd HH:mm:ss,SSSZ
        char[] array = line.toCharArray();
        if(        isDigit(array[0])  // y
                && isDigit(array[1])  // y
                && isDigit(array[2])  // y
                && isDigit(array[3])  // y
                && (array[4] == '-' || array[4] == '.')    // -
                && isDigit(array[5])  // M
                && isDigit(array[6])  // M
                && (array[7] == '-' || array[7] == '.')    // -
                && isDigit(array[8])  // d
                && isDigit(array[9])  // d
                && array[10] == ' '   //
                && isDigit(array[11]) // H
                && isDigit(array[12]) // H
                && array[13] == ':'   // :
                && isDigit(array[14]) // m
                && isDigit(array[15]) // m
                && array[16] == ':'   //
                && isDigit(array[17]) // s
                && isDigit(array[18]) // s
                && (array[19] == ',' || array[19] == '.')  //
                && isDigit(array[20]) // ms
                && isDigit(array[21]) // ms
                && isDigit(array[22]) // ms
                && (array[23] == '-' || array[23] == '+')  //
                && isDigit(array[24]) // tz
                && isDigit(array[25]) // tz
                && array[26] == ':'   //
                && isDigit(array[27]) // tz
                && isDigit(array[28]) // tz
        ) {
            LocalDateTime localDateTime = LocalDateTime.of(
                    Integer.parseInt(line.substring(0, 4)) // yyyy
                    , Integer.parseInt(line.substring(5, 7)) // MM
                    , Integer.parseInt(line.substring(8, 10)) // dd
                    , Integer.parseInt(line.substring(11, 13)) // HH
                    , Integer.parseInt(line.substring(14, 16)) // mm
                    , Integer.parseInt(line.substring(17, 19)) // ss
                    , Integer.parseInt(line.substring(20, 23)) * 1_000_000
            );
            ZoneOffset zoneOffset = ZoneOffset.of(line.substring(23, 29));
            return localDateTime.atOffset(zoneOffset);
        } else {
            return null;
        }
    }

}
