package com.grundfos.pump.replace.services;

import org.springframework.stereotype.Service;

public interface ConvertUTCService {
    String convertTimeZone(String strDateTime, String timeZone );
}
