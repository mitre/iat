/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/

package org.mitre.iwp.web.model;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum EyeLabel {
    UNDEFINED(0),
    RIGHT(1),
    LEFT(2),
    EMPTY(3);

    private int value;
    private static Map<Integer, EyeLabel> map = new HashMap<>();

    EyeLabel(int value) {
        this.value = value;
    }

    @JsonValue
    public int toValue() {
        return ordinal();
    }

    static {
        for (EyeLabel label : EyeLabel.values()) {
            map.put(label.value, label);
        }
    }

    public static EyeLabel valueOf(int label) {
        return map.get(label);
    }

    public int getValue() {
        return value;
    }
}
