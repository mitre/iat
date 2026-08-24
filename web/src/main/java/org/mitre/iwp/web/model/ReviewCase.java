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

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.util.Date;
import java.util.List;

@Entity
public class ReviewCase {
    public ReviewCase(){

    }

    public ReviewCase(Long id, String title, String description, String priority, Date dueDate, Date createdDate, String createdBy, Date modifiedDate, String modifiedBy, List<String> tags, boolean isWholeCaseCompleted) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
        this.createdDate = createdDate;
        this.createdBy = createdBy;
        this.modifiedDate = modifiedDate;
        this.modifiedBy = modifiedBy;
        this.tags = tags;
        this.isWholeCaseCompleted = isWholeCaseCompleted;
    }

    @Id
    @GeneratedValue
    private Long id;
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    private String title;
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    private String description;
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    private String priority;
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd", timezone="GMT")
    public Date dueDate;
    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd", timezone="GMT")
    public Date createdDate;
    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

    private String createdBy;
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd", timezone="GMT")
    public Date modifiedDate;
    public Date getModifiedDate() { return modifiedDate; }
    public void setModifiedDate(Date modifiedDate) { this.modifiedDate = modifiedDate; }

    private String modifiedBy;
    public String getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(String modifiedBy) { this.modifiedBy = modifiedBy; }

    @ElementCollection
    public List<String> tags;
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    private boolean isWholeCaseCompleted;
    public boolean getIsWholeCaseCompleted() { return isWholeCaseCompleted; }
    public void setIsWholeCaseCompleted(boolean isWholeCaseCompleted) { this.isWholeCaseCompleted = isWholeCaseCompleted; }

}
