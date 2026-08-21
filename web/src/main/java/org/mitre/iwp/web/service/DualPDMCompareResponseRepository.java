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

import org.springframework.data.jpa.repository.JpaRepository;
import org.mitre.iwp.web.model.DualPDMCompareResponse;

public interface DualPDMCompareResponseRepository extends JpaRepository<DualPDMCompareResponse, Long> {
    DualPDMCompareResponse findByProbeIdAndCandidateId(Long probeId, Long candidateId);
}
