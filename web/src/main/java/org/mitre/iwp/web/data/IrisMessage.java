/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

package org.mitre.iwp.web.data;

import org.mitre.jet.ebts.Ebts;
import org.mitre.jet.ebts.records.LogicalRecord;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class IrisMessage {
    @Id
    @GeneratedValue
    private Long id;
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    private String fileHash;
    public String getFileHash() { return this.fileHash; }
    public void setFileHash(String fileHash) { this.fileHash = fileHash; }

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Type1 type1;
    public Type1 getType1() { return type1; }
    public void setType1(Type1 type1){this.type1 = type1;}

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Type2 type2;
    public Type2 getType2() { return type2; }
    public void setType2(Type2 type2){this.type2 = type2;}

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Type17> type17 = new ArrayList<>();
    public List<Type17> getType17() { return type17; }
    public void setType17(List<Type17> t17){this.type17 = t17;}

    public IrisMessage() {
        // Empty constructor to satisfy JPA requirement
    }

    public String toString() {
        return String.format("%s<#id='%s'>",
                this.getClass().getSimpleName(),
                id);
    }

    public static IrisMessage buildFromEbts(Ebts ebts) {
        IrisMessage iMessage = new IrisMessage();
        for(LogicalRecord logicalRecord : ebts.getAllRecords()) {
            switch(logicalRecord.getRecordType()) {
                case 1:
                    iMessage.type1 = Type1.buildFromEbtsRecord(logicalRecord);
                    break;
                case 2:
                    iMessage.type2 = Type2.buildFromEbtsRecord(logicalRecord);
                    break;
                case 17:
                    if(iMessage.type17 != null) {
                        iMessage.type17.add(Type17.buildFromEbtsRecord(logicalRecord));
                    }
                    break;
                default:
                    break;
            }
        }

        return iMessage;
    }
}
