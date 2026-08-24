/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 
package org.mitre.iwp.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class IwpEbtsUtils {
    private static final String EBTS_DATE_FORMAT_STRING_JAVA = "yyyyMMdd";

    public static String convertToEbtsDate(Date date) {
        assert date != null : "date must not be null";
        SimpleDateFormat sdf = new SimpleDateFormat(EBTS_DATE_FORMAT_STRING_JAVA);
        return sdf.format(date);
    }

    public static Optional<Date> parseEbtsDate(String date) {
        assert date != null : "date must not be null";
        SimpleDateFormat sdf = new SimpleDateFormat(EBTS_DATE_FORMAT_STRING_JAVA);
        try {
            return Optional.of(sdf.parse(date));
        } catch(ParseException parseException) {
            return Optional.empty();
        }
    }
}
