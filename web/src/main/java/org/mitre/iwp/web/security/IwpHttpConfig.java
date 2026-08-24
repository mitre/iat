/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

package org.mitre.iwp.web.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IwpHttpConfig {
  private boolean isSecure;

  public IwpHttpConfig(@Value("${iwp.security.secured}") boolean isSecure) {
    this.isSecure = isSecure;
  }

  public boolean getIsSecure() {
    return isSecure;
  }
}


