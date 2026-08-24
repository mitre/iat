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

import java.util.HashMap;
import java.util.Map;

public enum ResponseType {
    Unknown(0),
    SRB(1),
    ERRB(2),
    XML(3),
    Image(4);

    private int value;
    private static Map<Integer, ResponseType> map = new HashMap<>();

    ResponseType(int value){
        this.value = value;
    }

    static {
        for(ResponseType label : ResponseType.values()){
            map.put(label.value, label);
        }
    }

    public static ResponseType valueOf(int label) {
        return map.get(label);
    }

    public int getValue() {
        return value;
    }
}
