package com.auction.common.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class TimeConverter {
    public static long toLong(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
