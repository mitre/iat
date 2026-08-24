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

import org.mitre.iwp.web.model.CreateIrisSearchRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreateIrisSearchRequestRepository extends JpaRepository<CreateIrisSearchRequest, Long> {
    List<CreateIrisSearchRequest> findByUser_Id(long id, Pageable sort);

	Optional<CreateIrisSearchRequest> findById(Long id);

    Long countAllByUser_Id(long id);
}
