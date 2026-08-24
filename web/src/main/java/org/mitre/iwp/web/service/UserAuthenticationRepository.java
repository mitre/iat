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


import org.mitre.iwp.web.model.Profile;
import org.mitre.iwp.web.model.UserAuthentication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserAuthenticationRepository extends JpaRepository<UserAuthentication, Long> {
  UserAuthentication findByUsername(String username);
  
  @Modifying
  @Query("delete from UserAuthentication u where u.profile = :profile")
  @Transactional
  void deleteByProfile(@Param("profile") Profile profile);
}

