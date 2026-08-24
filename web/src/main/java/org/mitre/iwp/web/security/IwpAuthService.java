
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;

import org.mitre.iwp.web.model.UserAuthentication;
import org.mitre.iwp.web.service.UserAuthenticationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class IwpAuthService {

    private static final Logger logger = LoggerFactory.getLogger(IwpAuthService.class);

    private UserAuthenticationRepository uaRepository;

    // Booleans to override responses if needed
    private final boolean isSecure;

	@Value("${iwp.users.defaultAdminUsername}")
	private String defaultAdminUsername;

    public IwpAuthService(UserAuthenticationRepository uaRepository, @Value("${iwp.security.secured}") boolean isSecure) {
        this.uaRepository = uaRepository;
        this.isSecure = isSecure;
    }

    public UserAuthentication getUserById(Long id){
        return uaRepository.findById(id).orElse(null);
    }

    /**
     * Finds the UserAuthentication for current user
     * 
     * @param principal
     * @return
     * @throws UsernameNotFoundException
     */
    public UserAuthentication getCurrentUser(Principal principal) throws UsernameNotFoundException {

        // Default the returned user to default admin if we're not using logon 
        String principalName = (principal == null) ? null : principal.getName();
        String username = (!isSecure) ? defaultAdminUsername : principalName;

        logger.debug("Principal: {}", username);

        try {
            return uaRepository.findByUsername(username);
        }catch (NullPointerException | DataAccessException e){
            logger.error(e.getMessage());
        }

        throw new BadCredentialsException("Bad Credentials");
    }

    /**
     * Determines what username to return depending on environment variables
     * 
     * NOTE: Ideally this isn't actually used in the future as even in testing there
     * should be authentication via a token even when not used with a web GUI. For
     * now we will by pass on our own.
     * 
     * @param principal
     * @return principal username or testing username
     */
    public String getCurrentUsername(Principal principal) {
        String principalName = (principal == null) ? "" : principal.getName();
        return (!isSecure) ? defaultAdminUsername : principalName;
    }

}
