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

import org.mitre.iwp.web.dto.ProfileDTO;
import org.mitre.iwp.web.dto.UserAuthenticationDTO;
import org.mitre.iwp.web.exception.InvalidMessageException;
import org.mitre.iwp.web.model.Profile;
import org.mitre.iwp.web.model.UserAuthentication;
import org.mitre.iwp.web.model.UserSetting;
import org.mitre.iwp.web.service.UserAuthenticationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component(value="iwpUserDetailsService")
public class IwpUserDetailsService implements UserDetailsService {
  private static final Logger logger = LoggerFactory.getLogger(IwpUserDetailsService.class);

  private final UserAuthenticationRepository userAuthenticationRepository;

  private final PasswordEncoder passwordEncoder;

  public IwpUserDetailsService(UserAuthenticationRepository userAuthenticationRepository,
                               PasswordEncoder passwordEncoder) {
    this.userAuthenticationRepository = userAuthenticationRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public UserDetails loadUserByUsername(String userId)
    throws UsernameNotFoundException {

    UserAuthentication user = this.userAuthenticationRepository.findByUsername(userId);
    if( user == null) {
      logger.error("user not found: {}", userId);
      throw new UsernameNotFoundException("Invalid username or password");
    }

    List<String> roles = new ArrayList<>();
    for( String role : user.getRoles() ) {
      roles.add(role.replace("ROLE_", ""));
    }

    UserDetails ret = User.withUsername(user.getUsername())
                        .password(user.getPassword())
                        .roles(roles.toArray(new String[0]))
                        .build();

    logger.info("user found returning user");
    return ret;
  }

  public void addUser(UserAuthenticationDTO userDto, String username) throws InvalidMessageException {
    UserAuthentication ret = this.userAuthenticationRepository.findByUsername(username);

    if( ret != null ) {
      throw new InvalidMessageException("User already exists");
    }

    UserAuthentication userObject = convertUserDtoToObject(userDto); 

    userAuthenticationRepository.save(userObject);

	logger.info("Adding user with name: {}", userObject.getUsername());
  }

  public void deleteUser(long userId) throws InvalidMessageException {
    if(this.userAuthenticationRepository.existsById(userId)) {
      this.userAuthenticationRepository.deleteById(userId);
    }
    else {
      throw new InvalidMessageException("User does not exist");
    }
  }

  public void updatePassword(String userName, String password) throws InvalidMessageException {
    UserAuthentication user = this.userAuthenticationRepository.findByUsername(userName);

    if(user == null){
      throw new InvalidMessageException("User does not exist");
    } else {
      logger.info("Ua Found, Name {}", user.getId());
      user.setPassword(password);
      this.userAuthenticationRepository.save(user);
    }
  }

  private UserAuthentication convertUserDtoToObject(UserAuthenticationDTO userDto) {
    UserAuthentication userObject = new UserAuthentication();
    userObject.setUsername(userDto.getUsername());
    userObject.setRoles(userDto.getRoles());

    ProfileDTO profileDto = userDto.getProfile();
    // If the profile name wasn't set for that user, we set it to the user's username.
    String profileName = profileDto.getName().isEmpty() ? userDto.getUsername(): profileDto.getName();

    UserSetting userSetting = new UserSetting(profileDto.getUserSetting().getId(), profileDto.getUserSetting().getStrokeWidth(), profileDto.getUserSetting().getDrawColor(), profileDto.getUserSetting().getFillColor(), profileDto.getUserSetting().getNumHistory(), profileDto.getUserSetting().getTshepiiStrokeOpacity());

    Profile profile = new Profile(profileDto.getId(), profileName, profileDto.getDestinationAgencyIdentifier(), profileDto.getOriginatingAgencyIdentifier(), profileDto.getAttentionIndicator(), profileDto.getSourceAgency(), profileDto.getIsReportExclude(), profileDto.getFullName(), profileDto.getEmail(), profileDto.getPhoneNumber(), profileDto.getTitle(), profileDto.getDepartment(),
    profileDto.getAddress(), userSetting);

    userObject.setProfile(profile);

    String encryptPassword = this.passwordEncoder.encode(userDto.getPassword());
    userObject.setPassword(encryptPassword);

    return userObject;
  }
}
