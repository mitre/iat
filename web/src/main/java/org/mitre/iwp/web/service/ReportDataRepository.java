/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

package org.mitre.iwp.web.service;

import org.mitre.iwp.web.model.ReportData;
import org.mitre.iwp.web.model.UserAuthentication;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportDataRepository extends JpaRepository<ReportData, Long> {
    List<ReportData> findByCreatedBy_Id(long id, Pageable sort);
    List<ReportData> findByWorkedBy_Id(long id, Pageable sort);
    List<ReportData> findByCreatedBy(UserAuthentication ua, Pageable sort);
    List<ReportData> findByWorkedBy(UserAuthentication ua, Pageable sort);
    List<ReportData> findByCreatedByOrWorkedBy(UserAuthentication cua, UserAuthentication wua, Pageable sort);

    Long countAllByCreatedBy_Id(long id);
    Long countAllByWorkedBy_Id(long id);
    Long countAllByCreatedBy_IdOrWorkedBy_Id(long cid, long wid);
    Long countAllByCreatedBy(UserAuthentication ua);
    Long countAllByWorkedBy(UserAuthentication ua);
    Long countAllByCreatedByOrWorkedBy(UserAuthentication cua, UserAuthentication wua);

    List<ReportData>  findByFileName(String fileName);

}
