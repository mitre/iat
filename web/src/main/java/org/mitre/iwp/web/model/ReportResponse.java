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

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class ReportResponse {
    private Long userId;
    private String filePath;
    private Long reportDataId;

    @Id
    @GeneratedValue
    private Long id;
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId(){ return this.userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getReportDataId(){ return this.reportDataId; }
    public void setReportDataId(Long reportDataId) { this.reportDataId = reportDataId; }

    public String getFilePath() { return this.filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
}
