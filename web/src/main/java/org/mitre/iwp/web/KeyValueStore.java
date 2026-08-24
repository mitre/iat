/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

package org.mitre.iwp.web;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class KeyValueStore {
    private static final Map<String, Object> data = new HashMap<>();

    public static Integer getInteger(String key) {
        return (Integer)(data.get(key));
    }

    public static Double getDouble(String key) {
        return (Double)(data.get(key));
    }

    public static Object get(String key) {
        return data.get(key);
    }

    public static void put(Object value, String... keys) {
        if (keys.length > 0) {
            data.put(StringUtils.join(keys, ":"), value);
        }
    }

    public static void purge() {
        data.clear();
    }

    public static void delete(String key) {
        data.remove(key);
    }

    public static void deletePattern(String regex) {
        Set<String> keys = data.keySet();
        Pattern pattern = Pattern.compile(regex);
        for(String key : keys) {
            if(pattern.matcher(key).matches()) {
                data.remove(key);
            }
        }
    }
}
