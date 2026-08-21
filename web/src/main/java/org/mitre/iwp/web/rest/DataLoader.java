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

import org.mitre.iwp.web.IwpWebProperties;
import org.mitre.iwp.web.model.*;
import org.mitre.iwp.web.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private UserAuthenticationRepository userAuthenticationRepository;
    private PrivilegeRepository privilegeRepository;
    private UserRoleRepository userRoleRepository;
    private IwpWebProperties iwpWebProperties;

    private static final String CREATE_SEARCH_STRING = "CREATE_SEARCH";
    private static final String CREATE_REVIEW_STRING = "CREATE_REVIEW";
    private static final String CASE_REQ_SUBMIT_STRING = "CASE_REQ_SUBMIT";
    private static final String ADD_COMMENT_STRING = "ADD_COMMENT";
    private static final String ROLE_USER_STRING = "ROLE_USER";
    private static final String ROLE_CASE_WORKER_STRING = "ROLE_CASE_WORKER";
    private static final String ROLE_CASE_SUPER_STRING = "ROLE_CASE_SUPERVISOR";

	//Default admin values

	@Value("${iwp.users.defaultAdminFullName}")
	private String defaultAdminFullName;

	@Value("${iwp.users.defaultAdminTitle}")
	private String defaultAdminTitle;

	@Value("${iwp.users.defaultAdminEmail}")
	private String defaultAdminEmail;

	@Value("${iwp.users.defaultAdminUsername}")
	private String defaultAdminUsername;

	@Value("${iwp.users.defaultAdminPassword}")
	private String defaultAdminPassword;

	//Default reviewer values

	@Value("${iwp.users.defaultReviewerFullName}")
	private String defaultReviewerFullName;

	@Value("${iwp.users.defaultReviewerTitle}")
	private String defaultReviewerTitle;

	@Value("${iwp.users.defaultReviewerEmail}")
	private String defaultReviewerEmail;

	@Value("${iwp.users.defaultReviewerUsername}")
	private String defaultReviewerUsername;

	@Value("${iwp.users.defaultReviewerPassword}")
	private String defaultReviewerPassword;

	//Default supervisor values

	@Value("${iwp.users.defaultSupervisorFullName}")
	private String defaultSupervisorFullName;

	@Value("${iwp.users.defaultSupervisorTitle}")
	private String defaultSupervisorTitle;

	@Value("${iwp.users.defaultSupervisorEmail}")
	private String defaultSupervisorEmail;

	@Value("${iwp.users.defaultSupervisorUsername}")
	private String defaultSupervisorUsername;

	@Value("${iwp.users.defaultSupervisorPassword}")
	private String defaultSupervisorPassword;

    @Autowired
    public DataLoader(UserAuthenticationRepository userAuthenticationRepository,
                      PrivilegeRepository privilegeRepository,
                      UserRoleRepository userRoleRepository,
                      IwpWebProperties iwpWebProperties){
        this.userAuthenticationRepository = userAuthenticationRepository;
        this.privilegeRepository = privilegeRepository;
        this.userRoleRepository = userRoleRepository;
        this.iwpWebProperties = iwpWebProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if(this.privilegeRepository.count() == 0){
            log.info("Adding Privileges");
            this.privilegeRepository.save(new Privilege("USER_EDIT", "Can add,edit,remove Users from system"));
            this.privilegeRepository.save(new Privilege("CASE_EDIT","Can create and edit Cases"));
            this.privilegeRepository.save(new Privilege("CASE_DELETE","Can delete Cases"));
            this.privilegeRepository.save(new Privilege("CASE_REPORTS","Can generate reports on Cases"));
            this.privilegeRepository.save(new Privilege(CREATE_SEARCH_STRING,"Can create, edit and submit a search, not associated with any Case"));
            this.privilegeRepository.save(new Privilege(CREATE_REVIEW_STRING,"Can create, edit and submit a review, not associated with any Case"));
            this.privilegeRepository.save(new Privilege("CASE_REQ_ASSIGN_ANY","Can assign Case Request to self or others"));
            this.privilegeRepository.save(new Privilege("CASE_REQ_ASSIGN_REVIEW_TO_ME","Can assign unassigned Review Case Requests to self"));
            this.privilegeRepository.save(new Privilege("CASE_REQ_ASSIGN_SEARCH_TO_ME","Can assign unassigned Search Case Requests to self"));
            this.privilegeRepository.save(new Privilege("CASE_REQ_EDIT","Can edit Case Requests"));
            this.privilegeRepository.save(new Privilege("CASE_REQ_ADD","Can add a Case Request to Case"));
            this.privilegeRepository.save(new Privilege(CASE_REQ_SUBMIT_STRING,"Can Submit a Case Request"));
            this.privilegeRepository.save(new Privilege("CASE_REQ_CLOSE","Can close a Case Request"));
            this.privilegeRepository.save(new Privilege("CASE_REQ_DELETE","Can delete a Case Request"));
            this.privilegeRepository.save(new Privilege(ADD_COMMENT_STRING,"Can add comment to a Case"));
            this.privilegeRepository.save(new Privilege("DELETE_COMMENT","Can delete own comment from Case"));
            this.privilegeRepository.save(new Privilege("DELETE_COMMENT_ANY","Can delete others comments from Case"));
        }

        if(this.userRoleRepository.count() == 0){
            log.info("Adding Use Roles");
            UserRole user = new UserRole();
            user.setName(ROLE_USER_STRING);
            user.setDescription("Everyone");
            this.userRoleRepository.save(user);

            UserRole userCaseWorker = new UserRole();
            userCaseWorker.setName(ROLE_CASE_WORKER_STRING);
            userCaseWorker.setDescription("Case Worker");
            userCaseWorker.getPrivileges().add(this.privilegeRepository.findByName(CREATE_REVIEW_STRING));
            userCaseWorker.getPrivileges().add(this.privilegeRepository.findByName(CREATE_SEARCH_STRING));
            userCaseWorker.getPrivileges().add(this.privilegeRepository.findByName("CASE_REQ_ASSIGN_REVIEW_TO_ME"));
            userCaseWorker.getPrivileges().add(this.privilegeRepository.findByName("CASE_REQ_ASSIGN_SEARCH_TO_ME"));
            userCaseWorker.getPrivileges().add(this.privilegeRepository.findByName(CASE_REQ_SUBMIT_STRING));
            userCaseWorker.getPrivileges().add(this.privilegeRepository.findByName(ADD_COMMENT_STRING));
            userCaseWorker.getPrivileges().add(this.privilegeRepository.findByName("DELETE_COMMENT"));
            this.userRoleRepository.save(userCaseWorker);

            UserRole userAdmin = new UserRole();
            userAdmin.setName("ROLE_ADMIN");
            userAdmin.setDescription("Administrator");
            userAdmin.getPrivileges().add(this.privilegeRepository.findByName("USER_EDIT"));
            this.userRoleRepository.save(userAdmin);

            UserRole userSuper = new UserRole();
            userSuper.setName(ROLE_CASE_SUPER_STRING);
            userSuper.setDescription("Case Supervisors");
            userSuper.getPrivileges().add(this.privilegeRepository.findByName("CASE_EDIT"));
            userSuper.getPrivileges().add(this.privilegeRepository.findByName("CASE_DELETE"));
            userSuper.getPrivileges().add(this.privilegeRepository.findByName("CASE_REPORTS"));
            userSuper.getPrivileges().add(this.privilegeRepository.findByName(CREATE_REVIEW_STRING));
            userSuper.getPrivileges().add(this.privilegeRepository.findByName(CREATE_SEARCH_STRING));
            userSuper.getPrivileges().add(this.privilegeRepository.findByName("CASE_REQ_ASSIGN_ANY"));
            userSuper.getPrivileges().add(this.privilegeRepository.findByName("CASE_REQ_EDIT"));
            userSuper.getPrivileges().add(this.privilegeRepository.findByName("CASE_REQ_ADD"));
            userSuper.getPrivileges().add(this.privilegeRepository.findByName(CASE_REQ_SUBMIT_STRING));
            userSuper.getPrivileges().add(this.privilegeRepository.findByName("CASE_REQ_CLOSE"));
            userSuper.getPrivileges().add(this.privilegeRepository.findByName("CASE_REQ_DELETE"));
            userSuper.getPrivileges().add(this.privilegeRepository.findByName(ADD_COMMENT_STRING));
            userSuper.getPrivileges().add(this.privilegeRepository.findByName("DELETE_COMMENT_ANY"));

            this.userRoleRepository.save(userSuper);
        }

        if(this.userAuthenticationRepository.count() == 0){
            log.info("Adding User Authentication");
            
			UserAuthentication userTest = new UserAuthentication();
			log.debug("CREATING DEFAULT ADMIN USER WITH defaultAdminUsername: {}, defaultAdminFullName: {}, defaultAdminTitle: {}, defaultAdminEmail: {}", defaultAdminUsername,  defaultAdminFullName, defaultAdminTitle, defaultAdminEmail);
            userTest.setUsername(defaultAdminUsername);
            userTest.setPassword(defaultAdminPassword);
            userTest.setProfile(getDefaultProfile(defaultAdminUsername, defaultAdminFullName, defaultAdminTitle, defaultAdminEmail));
            //"ROLE_USER", "ROLE_CASE_WORKER", "ROLE_ADMIN", "ROLE_CASE_SUPERVISOR"
            userTest.getRoles().add(ROLE_USER_STRING);
            userTest.getRoles().add(ROLE_CASE_WORKER_STRING);
            userTest.getRoles().add("ROLE_ADMIN");
            userTest.getRoles().add(ROLE_CASE_SUPER_STRING);
            this.userAuthenticationRepository.save(userTest);

            UserAuthentication userReviewer = new UserAuthentication();
			log.debug("CREATING DEFAULT REVIEWER USER WITH defaultReviewerUsername: {}, defaultReviewerFullName: {}, defaultReviewerTitle: {}, defaultReviewerEmail: {}", defaultReviewerUsername, defaultReviewerFullName, defaultReviewerTitle, defaultReviewerEmail);
            userReviewer.setUsername(defaultReviewerUsername);
            userReviewer.setPassword(defaultReviewerPassword);
            userReviewer.setProfile(getDefaultProfile(defaultReviewerUsername, defaultReviewerFullName, defaultReviewerTitle, defaultReviewerEmail));
            //"ROLE_USER","ROLE_CASE_WORKER"
            userReviewer.getRoles().add(ROLE_USER_STRING);
            userReviewer.getRoles().add(ROLE_CASE_WORKER_STRING);
            this.userAuthenticationRepository.save(userReviewer);

            UserAuthentication userSuper = new UserAuthentication();
			log.debug("CREATING DEFAULT SUPERVISOR USER WITH defaultSupervisorUsername: {}, defaultSupervisorFullName: {}, defaultSupervisorTitle: {}, defaultSupervisorEmail: {}", defaultSupervisorUsername, defaultSupervisorFullName, defaultSupervisorTitle, defaultSupervisorEmail);
            userSuper.setUsername(defaultSupervisorUsername);
            userSuper.setPassword(defaultSupervisorPassword);
            userSuper.setProfile(getDefaultProfile(defaultSupervisorUsername, defaultSupervisorFullName, defaultSupervisorTitle, defaultSupervisorEmail));
            //"ROLE_USER","ROLE_CASE_SUPERVISOR"
            userSuper.getRoles().add(ROLE_USER_STRING);
            userSuper.getRoles().add(ROLE_CASE_SUPER_STRING);
            this.userAuthenticationRepository.save(userSuper);
        }


    }

    private UserSetting getDefaultUserSetting(){
        UserSetting us = new UserSetting();
        us.setStrokeWidth(this.iwpWebProperties.getProfileStrokeWidth());
        us.setDrawColor(this.iwpWebProperties.getProfileDrawColor());
        us.setFillColor(this.iwpWebProperties.getProfileFillColor());
        us.setNumHistory(this.iwpWebProperties.getProfileNumberHistory());
        return us;
    }

    private Profile getDefaultProfile(String name, String fullName, String title, String email){
        Profile prof = new Profile();
        prof.setUserSetting(this.getDefaultUserSetting());
        prof.setName(name);
        prof.setFullName(fullName);
        prof.setTitle(title);
        prof.setEmail(email);
        prof.setDestinationAgencyIdentifier(this.iwpWebProperties.getProfileDai());
        prof.setOriginatingAgencyIdentifier(this.iwpWebProperties.getProfileOri());
        prof.setAttentionIndicator(this.iwpWebProperties.getProfileAttIndicator());
        prof.setSourceAgency(this.iwpWebProperties.getProfileSrcAgency());
        prof.setIsReportExclude(this.iwpWebProperties.getIsProfileReportExclude());
        prof.setDepartment(this.iwpWebProperties.getProfileDpt());
        prof.setAddress(this.iwpWebProperties.getProfileAdd());

        return prof;
    }
}
