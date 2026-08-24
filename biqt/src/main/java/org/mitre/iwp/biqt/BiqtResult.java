/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

package org.mitre.iwp.biqt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BiqtResult {

    private String provider;

    public String getProvider() {
        return provider;
    }

    private long errorCode;

    public long getErrorCode() {
        return errorCode;
    }

    private String message;

    public String getMessage() {
        return message;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QualityResult {

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Metrics {
            private double quality;

            public double getQuality() {
                return quality;
            }

            private double cosmeticContactConfidence;

            @JsonProperty("cosmetic_contact_confidence")
            public double getCosmeticContactConfidence() {
                return cosmeticContactConfidence;
            }
        }

        private Metrics metrics;

        public Metrics getMetrics() {
            return metrics;
        }
    }

    private List<QualityResult> qualityResult;

    public List<QualityResult> getQualityResult() {
        return qualityResult;
    }
}
