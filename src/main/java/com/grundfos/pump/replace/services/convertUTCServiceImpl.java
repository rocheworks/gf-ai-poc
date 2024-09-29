package com.grundfos.pump.replace.services;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Qualifier("convertUTCServiceImpl")
@Service
public class convertUTCServiceImpl implements ConvertUTCService {
    private static final DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
    @Override
    public String convertTimeZone(String strDateTime, String strTimezone) {
        try{
            LocalDateTime localDateTime = LocalDateTime.parse(strDateTime, inputFormatter);
            ZoneId zoneId = ZoneId.of(strTimezone);
            ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, zoneId);
            ZonedDateTime utcDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"));
            return outputFormatter.format(utcDateTime);
        }catch (Exception e){
            e.printStackTrace();
            return "Invalid input date-time or time zone";
        }
    }
}
