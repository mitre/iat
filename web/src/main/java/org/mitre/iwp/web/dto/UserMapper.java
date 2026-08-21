/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

package org.mitre.iwp.web.dto;

import org.mitre.iwp.web.model.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;

@Component
public class UserMapper {
    public AppNotificationDTO toAppNotificationDTO(AppNotification appNotification){
        if(appNotification == null){
            return new AppNotificationDTO();
        }

        return new AppNotificationDTO(appNotification.getId(),
                appNotification.getUserId(),
                appNotification.getMessage(),
                appNotification.getIcon(),
                appNotification.getAction(),
                appNotification.getDateCreated(),
                appNotification.getIsNew());
    }

    public AppNotification toAppNotification(AppNotificationDTO appNotification){
        if(appNotification == null){
            return new AppNotification();
        }

        return new AppNotification(appNotification.getId(),
                appNotification.getUserId(),
                appNotification.getMessage(),
                appNotification.getIcon(),
                appNotification.getAction(),
                appNotification.getDateCreated(),
                appNotification.isNew());
    }

    public UserAuthenticationDTO toUserAuthenticationDTO(UserAuthentication userAuthentication){
        if(userAuthentication == null){
            return null;
        }

        ProfileDTO profileDto = this.toProfileDto(userAuthentication.getProfile());

        return new UserAuthenticationDTO(userAuthentication.getId(), userAuthentication.getUsername(), userAuthentication.getPassword(),
                userAuthentication.getRoles(), profileDto, true);
    }

    public UserAuthentication toUserAuthentication(UserAuthenticationDTO userAuthenticationDTO){
        if(userAuthenticationDTO == null){
            return null;
        }

        Profile profile = this.toProfile(userAuthenticationDTO.profile);

        return new UserAuthentication(userAuthenticationDTO.getId(), userAuthenticationDTO.username, userAuthenticationDTO.password,
                userAuthenticationDTO.roles, profile, userAuthenticationDTO.isNewUser);
    }

    public ProfileDTO toProfileDto(Profile profile){
        if(profile == null){
            return new ProfileDTO();
        }

        UserSettingDTO userSettingDto = this.toUserSettingDto(profile.getUserSetting());
        return new ProfileDTO(profile.getId(), profile.getName(), profile.getDestinationAgencyIdentifier(),
                profile.getOriginatingAgencyIdentifier(), profile.getAttentionIndicator(), profile.getSourceAgency(),
                profile.getIsReportExclude(), profile.getFullName(), profile.getEmail(), profile.getPhoneNumber(),
                profile.getTitle(), profile.getDepartment(), profile.getAddress(), userSettingDto);
    }

    public Profile toProfile(ProfileDTO profileDto){
        if(profileDto == null){
            return new Profile();
        }

        UserSetting userSetting = this.toUserSetting(profileDto.userSetting);
        return new Profile(profileDto.getId(), profileDto.getName(), profileDto.getDestinationAgencyIdentifier(),
                profileDto.getOriginatingAgencyIdentifier(), profileDto.getAttentionIndicator(), profileDto.getSourceAgency(),
                profileDto.getIsReportExclude(), profileDto.getFullName(), profileDto.getEmail(), profileDto.getPhoneNumber(),
                profileDto.getTitle(), profileDto.getDepartment(), profileDto.getAddress(),userSetting);
    }

    public UserSettingDTO toUserSettingDto(UserSetting userSetting){
        if(userSetting == null){
            return new UserSettingDTO();
        }

        return new UserSettingDTO(userSetting.getId(), userSetting.getStrokeWidth(), userSetting.getDrawColor(),
                userSetting.getFillColor(), userSetting.getNumHistory(), userSetting.getTshepiiStrokeOpacity());
    }

    public UserSetting toUserSetting(UserSettingDTO userSettingDto){
        if(userSettingDto == null){
            return new UserSetting();
        }

        return new UserSetting(userSettingDto.getId(), userSettingDto.getStrokeWidth(), userSettingDto.getDrawColor(),
                userSettingDto.getFillColor(), userSettingDto.getNumHistory(), userSettingDto.getTshepiiStrokeOpacity());
    }

    public UserRoleDTO toUserRoleDto(UserRole userRole){
        if(userRole == null){
            return new UserRoleDTO();
        }

        Collection<PrivilegeDTO> privilegeDTOCollection = new ArrayList<>();
        for(Privilege p : userRole.getPrivileges()){
            privilegeDTOCollection.add(this.toPrivilegeDTO(p));
        }

        Collection<UserAuthenticationDTO> userAuthenticationDTOCollection = new ArrayList<>();
        for(UserAuthentication ua : userRole.getUsers()){
            userAuthenticationDTOCollection.add(this.toUserAuthenticationDTO(ua));
        }

        return new UserRoleDTO(userRole.getId(), userRole.getName(), userRole.getDescription(), userAuthenticationDTOCollection, privilegeDTOCollection);
    }

    public UserRole toUserRole(UserRoleDTO userRoleDTO){
        if(userRoleDTO == null){
            return new UserRole();
        }

        Collection<Privilege> privilegeCollection = new ArrayList<>();
        for(PrivilegeDTO p : userRoleDTO.getPrivileges()){
            privilegeCollection.add(this.toPrivilege(p));
        }

        Collection<UserAuthentication> userAuthenticationCollection = new ArrayList<>();
        for(UserAuthenticationDTO ua : userRoleDTO.getUsers()){
            userAuthenticationCollection.add(this.toUserAuthentication(ua));
        }

        return new UserRole(userRoleDTO.getId(), userRoleDTO.getName(), userRoleDTO.getDescription(), userAuthenticationCollection, privilegeCollection);
    }

    public PrivilegeDTO toPrivilegeDTO(Privilege privilege){
        if(privilege == null){
            return new PrivilegeDTO();
        }

        return new PrivilegeDTO(privilege.getId(), privilege.getName(), privilege.getDescription());
    }

    public Privilege toPrivilege(PrivilegeDTO privilegeDto){
        if(privilegeDto == null){
            return new Privilege();
        }

        return new Privilege(privilegeDto.getId(), privilegeDto.getName(), privilegeDto.getDescription());
    }
}
