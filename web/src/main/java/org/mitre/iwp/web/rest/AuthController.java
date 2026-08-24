/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

package org.mitre.iwp.web.rest;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.apache.commons.collections4.IterableUtils;
import org.mitre.iwp.web.dto.*;
import org.mitre.iwp.web.model.Privilege;
import org.mitre.iwp.web.model.UserAuthentication;
import org.mitre.iwp.web.model.UserRole;
import org.mitre.iwp.web.security.IwpAuthService;
import org.mitre.iwp.web.security.IwpHttpConfig;
import org.mitre.iwp.web.security.IwpUserDetailsService;
import org.mitre.iwp.web.service.NotificationService;
import org.mitre.iwp.web.service.PrivilegeRepository;
import org.mitre.iwp.web.service.UserAuthenticationRepository;
import org.mitre.iwp.web.service.UserRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final IwpUserDetailsService iwpUserDetailsService;

    private final NotificationService notificationService;

    private final UserAuthenticationRepository userAuthenticationRepository;

    private final UserRoleRepository roleRepository;

    private final PrivilegeRepository privilegeRepository;

    private final UserMapper userMapper;

    private IwpAuthService authService;
    private IwpHttpConfig iwpHttpConfig;
	private final PasswordEncoder passwordEncoder;

    private static final String ERROR_DELETE_MESSAGE = "Error deleting privilege";
    private static final String ERROR_UPDATE_MESSAGE = "Error updating password";
    private static final String ERROR_INCORRECT_PASSWORD_MESSAGE = "Incorrect Password for User";

    public AuthController(IwpUserDetailsService iwpUserDetailsService, NotificationService notificationService,
            UserAuthenticationRepository userAuthenticationRepository, UserRoleRepository roleRepository,
            PrivilegeRepository privilegeRepository, UserMapper userMapper, IwpHttpConfig iwpHttpConfig,
                          IwpAuthService authService, PasswordEncoder passwordEncoder) {
        this.iwpUserDetailsService = iwpUserDetailsService;
        this.notificationService = notificationService;
        this.userAuthenticationRepository = userAuthenticationRepository;
        this.roleRepository = roleRepository;
        this.privilegeRepository = privilegeRepository;
        this.authService = authService;
        this.userMapper = userMapper;
        this.iwpHttpConfig = iwpHttpConfig;
		this.passwordEncoder = passwordEncoder;
    }

    @GetMapping(value = "/api/logon/info")
    public Boolean getIsSecure() {
        return iwpHttpConfig.getIsSecure();
    }

    @Secured({ "ROLE_USER" })
    @GetMapping(value = "/api/admin/user", produces = { MediaType.TEXT_PLAIN_VALUE })
    @ResponseBody
    public String getUser(Principal principal) {
        return authService.getCurrentUsername(principal);
    }

    @Secured({ "ROLE_ADMIN" })
    @GetMapping(value = "/api/admin/users")
    @ResponseBody
    public Collection<UserAuthentication> getAllUsers() {
        List<UserAuthentication> uaList = IterableUtils.toList(userAuthenticationRepository.findAll());

        for (UserAuthentication ua : uaList) {
            ua.setPassword("");
        }

        return uaList;
    }

    @Secured({ "ROLE_CASE_SUPERVISOR" })
    @GetMapping(value = "/api/admin/userList")
    @ResponseBody
    public Collection<String> getAllUsernames() {
        List<UserAuthentication> users = IterableUtils.toList(userAuthenticationRepository.findAll());
        return users.stream().map(UserAuthentication::getUsername).collect(Collectors.toList());
    }

    @Secured({ "ROLE_ADMIN" })
    @PostMapping(value = "/api/admin/user/add/update/roles")
    public ResponseEntity<String> addUser(@RequestBody UserAuthenticationDTO userObject) {
        try {
            if (userObject.getId() == -1 && userObject.getIsNewUser()) {
                iwpUserDetailsService.addUser(userObject, userObject.getUsername());
            } else {
                UserAuthentication existingUser = userAuthenticationRepository.findByUsername(userObject.getUsername());

                existingUser.setRoles(userObject.getRoles());

                userAuthenticationRepository.save(existingUser);
            }
            return ResponseEntity.ok().body("OK");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding user");
        }
    }

    @Secured({ "ROLE_ADMIN" })
    @PostMapping(value = "/api/admin/user/delete")
    public ResponseEntity<String> deleteUser(@RequestBody UserAuthenticationDTO userObject) {
        try {
            iwpUserDetailsService.deleteUser(userObject.getId());
            return ResponseEntity.ok().body("OK");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting user");
        }
    }

    @Secured({"ROLE_USER"})
    @PostMapping(value = "/api/admin/check/password")
    public ResponseEntity<String> checkPassword(@RequestBody UserPasswordData userPasswordData) {
        try {
            UserDetails userDetails = iwpUserDetailsService.loadUserByUsername(userPasswordData.userName);
            if (passwordEncoder.matches(userPasswordData.oldPassword, userDetails.getPassword())) {
                logger.info("Passwords match. Calling updatePassword");
                return ResponseEntity.ok().body("OK");
            } else {
                logger.info("PASSWORDS DID NOT MATCH.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_INCORRECT_PASSWORD_MESSAGE);
            }
        } catch (Exception e) {
			logger.error("ERROR OCCURED DURING PASSWORD CHANGE.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_UPDATE_MESSAGE);
        }
    }

    @Secured({ "ROLE_USER" })
    @PostMapping(value = "/api/admin/user/password")
    public ResponseEntity<String> updateUserPassword(@RequestBody UserPasswordData userPasswordData) {
        try {
			String newPasswordEncrypt = passwordEncoder.encode(userPasswordData.newPassword);
            // Don't need to check if the current password is the same because it gets checked on the frontend before this method is called.
            iwpUserDetailsService.updatePassword(userPasswordData.userName, newPasswordEncrypt);
            return ResponseEntity.ok().body("OK");
        } catch (Exception e) {
			logger.error("ERROR OCCURED DURING PASSWORD CHANGE.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_UPDATE_MESSAGE);
        }
    }

    public static class UserPasswordData {
        String userName;

        public void setUserName(String userName) {
            this.userName = userName;
        }

        String oldPassword;

        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }

        String newPassword;

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }

    /***
     *
     * @return list of UserNotifications for this user
     */
    @Secured({ "ROLE_USER" })
    @GetMapping(value = "/api/admin/user/notifications")
    public ResponseEntity<?> getUserNotifications(Principal principal) {
        UserAuthentication ua;
        try {
            ua = authService.getCurrentUser(principal);
        } catch (UsernameNotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(notificationService.getNotificationsForUser(ua.getId()), HttpStatus.OK);
    }

    /***
     * @return count of UserNotifications for this user
     */
    @Secured({ "ROLE_USER" })
    @GetMapping(value = "/api/admin/user/notifications/count")
    public ResponseEntity<?> getUserNotificationCount(Principal principal) {
        UserAuthentication ua;
        try {
            ua = authService.getCurrentUser(principal);
        } catch (UsernameNotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(notificationService.getNotificationCountForUser(ua.getId()), HttpStatus.OK);
    }


    @Secured({ "ROLE_USER" })
    @GetMapping(value = "/api/admin/user/profile")
    public ResponseEntity<?> getProfile(Principal principal) {
        UserAuthentication ua;
        try {
            ua = authService.getCurrentUser(principal);
        } catch (UsernameNotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(ua.getProfile(), HttpStatus.OK);
    }

    // assumes that the profile is already there since it is part of the
    // UserAuthentication
    @Secured({ "ROLE_USER" })
    @PostMapping(value = "/api/admin/user/profile")
    public ResponseEntity<?> setProfile(@RequestBody ProfileDTO profile, Principal principal) {
        UserAuthentication ua;
        try {
            ua = authService.getCurrentUser(principal);
        } catch (UsernameNotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
        }

        ua.setProfile(this.userMapper.toProfile(profile));
        userAuthenticationRepository.save(ua);

        return new ResponseEntity<>("Profile saved", HttpStatus.OK);
    }

    /// Roles ///

    /***
     * get all the roles in system
     * 
     * @return
     */
    @Secured({ "ROLE_ADMIN" })
    @GetMapping(value = "/api/admin/roles")
    @ResponseBody
    public Collection<UserRole> getAllUserRole() {
        logger.info("/api/admin/roles");
        return IterableUtils.toList(roleRepository.findAll());
    }

    /**
     * Get all the roles for the current user
     * 
     * @return
     */
    @Secured({ "ROLE_USER" })
    @GetMapping(value = "/api/admin/user/roles")
    public ResponseEntity<?> getUserRolesForUser(Principal principal) {

        logger.info("/api/admin/user/roles");

        UserAuthentication ua;
        try {
            ua = authService.getCurrentUser(principal);
        } catch (UsernameNotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
        }

        List<String> roles = ua.getRoles();
        logger.debug("user auth roles: {}", roles);

        Collection<UserRole> ret = roleRepository.findByNameIn(roles);
        logger.debug("user roles: {}", ret);

        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

    /***
     * add user to a role by role name
     * 
     * @param roleName
     * @return
     */
    @Secured({ "ROLE_ADMIN" })
    @GetMapping(value = "/api/admin/user/role/{roleName}")
    public ResponseEntity<?> addUserRoleToUser(@PathVariable("roleName") String roleName, Principal principal) {
        logger.info("/api/admin/user/role/{}", roleName);

        UserAuthentication ua;
        try {
            ua = authService.getCurrentUser(principal);
        } catch (UsernameNotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
        }

        UserRole role = null;
        try {
            role = roleRepository.findByName(roleName);
        } catch (NoSuchElementException ex) {
            return new ResponseEntity<>("No such role exists under this name", HttpStatus.BAD_REQUEST);
        }

        role.addUser(ua);
        roleRepository.save(role);
        return new ResponseEntity<>(role, HttpStatus.OK);
    }

    /***
     * add a privilege to a role
     * 
     * @param roleName
     * @param privilegeName
     * @return
     */
    @Secured({ "ROLE_ADMIN" })
    @GetMapping(value = "/api/admin/role/{roleName}/privilege/{privilegeName}")
    @ResponseBody
    public UserRole addPrivilegeToRole(@PathVariable("roleName") String roleName,
            @PathVariable("privilegeName") String privilegeName) {
        logger.info("/api/admin/role/{}/privilege/{}", roleName, privilegeName);
        UserRole role = roleRepository.findByName(roleName);
        Privilege priv = privilegeRepository.findByName(privilegeName);
        role.addPrivilege(priv);
        roleRepository.save(role);
        return role;
    }

    @Secured({ "ROLE_ADMIN" })
    @DeleteMapping(value = "/api/admin/role/{roleName}/privilege/{privilegeName}")
    @ResponseBody
    public UserRole removePrivilegeFromRole(@PathVariable("roleName") String roleName,
            @PathVariable("privilegeName") long privilegeName) {
        logger.info("/api/admin/role/{}/privilege/{}", roleName, privilegeName);
        UserRole role = roleRepository.findByName(roleName);
        role.removePrivilege(privilegeName);
        roleRepository.save(role);
        return role;
    }

    /***
     * get a role
     * 
     * @param roleName
     * @return
     */
    @Secured({ "ROLE_ADMIN" })
    @GetMapping(value = "/api/admin/role/{roleName}")
    @ResponseBody
    public UserRole getUserRole(@PathVariable("roleName") String roleName) {
        logger.info("/api/admin/role/{}", roleName);
        return roleRepository.findByName(roleName);
    }

    @Secured({ "ROLE_ADMIN" })
    @PostMapping(value = "/api/admin/role/", produces = { MediaType.TEXT_PLAIN_VALUE })
    @ResponseBody
    public UserRole addRole(@RequestBody UserRoleDTO role) {
        logger.info("/api/admin/role/ addRole");

        try {
            UserRole exist = roleRepository.findByName(role.getName());

            if (exist == null) {
                return roleRepository.save(this.userMapper.toUserRole(role));
            }
        } catch (Exception ex) {
            logger.error(ERROR_DELETE_MESSAGE, ex);
        }
        return null;
    }

    @Secured({ "ROLE_ADMIN" })
    @DeleteMapping(value = "/api/admin/role/{id}")
    public ResponseEntity<String> deleteRole(@PathVariable("id") long id) {
        logger.debug("/api/admin/role/{} DELETE", id);
        try {
            roleRepository.deleteById(id);
            return ResponseEntity.ok().body(String.format("role '%s' deleted.", id));

        } catch (Exception ex) {
            logger.error("Error deleting role", ex);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting role");
    }

    /// PRIVILEGES ///

    /***
     * get the privileges for the current user
     * 
     * @return
     */
    @Secured({ "ROLE_USER" })
    @GetMapping(value = "/api/admin/user/privileges")
    public ResponseEntity<?> getUserPrivileges(Principal principal) {
        UserAuthentication ua;
        try {
            ua = authService.getCurrentUser(principal);
        } catch (UsernameNotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
        }

        List<UserRole> roles = roleRepository.findByNameIn(ua.getRoles());
        List<String> privs = new ArrayList<>();

        for (UserRole r : roles) {
            privs.addAll(r.getPrivilegeList());
        }
        return new ResponseEntity<>(privs, HttpStatus.OK);
    }

    /***
     * get list of all the available privileges
     * 
     * @return
     */
    @Secured({ "ROLE_ADMIN" })
    @GetMapping(value = "/api/admin/privilege/")
    @ResponseBody
    public Collection<Privilege> getAllPrivileges() {
        logger.info("/api/admin/privilege/");
        return IterableUtils.toList(privilegeRepository.findAll());
    }

    @Secured({ "ROLE_ADMIN" })
    @GetMapping(value = "/api/admin/privilege/{name}")
    @ResponseBody
    public Privilege getPrivilege(@PathVariable("name") String name) {
        logger.info("/api/admin/privilege/name {}", name);
        return privilegeRepository.findByName(name);
    }

    @Secured({ "ROLE_ADMIN" })
    @ResponseBody
    @PostMapping(value = "/api/admin/privilege/", produces = { MediaType.TEXT_PLAIN_VALUE })
    public Privilege addPrivilege(@RequestBody PrivilegeDTO priv) {
        return privilegeRepository.save(this.userMapper.toPrivilege(priv));
    }

    @Secured({ "ROLE_ADMIN" })
    @DeleteMapping(value = "/api/admin/privilege/{id}")
    public ResponseEntity<String> deletePrivilege(@PathVariable("id") long id) {
        logger.debug("/api/admin/priviledge/{} DELETE", id);
        try {
            privilegeRepository.deleteById(id);
            return ResponseEntity.ok().body(String.format("privilege '%s' deleted.", id));

        } catch (Exception ex) {
            logger.error(ERROR_DELETE_MESSAGE, ex);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_DELETE_MESSAGE);
    }

}
