/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

package org.mitre.iwp.web.data;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mitre.iwp.web.data.config.AbstractInitializer;
import org.mitre.iwp.web.exception.InvalidMessageException;
import org.mitre.iwp.web.model.Profile;
import org.mitre.iwp.web.service.IrisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = {"classpath:application-test.properties", "classpath:application.properties"}, 
    properties = { "iwp.security.secured=false", "spring.main.allow-circular-references=true" })
@Transactional
public class ProfileTests extends AbstractInitializer {

    @Autowired
    private IrisService irisService;

    Profile testProfile = null;

    @Test
    public void TestProfiles() {
        try{
            clearProfiles();
        } catch (Exception e) {
            Assertions.assertTrue(false); // should not fail
        }

        try {
            ProfileTest();
        }catch(Exception e){
            Assertions.assertTrue(false); // should not fail
        }

        try {
            ProfileDuplicationTest();
            Assertions.assertTrue(false); // should fail
        }catch(Exception e){
            Assertions.assertEquals("Profile name 'Profile 1' already exists in system", e.getMessage());
        }

        ProfileListTest();

        try {
            ProfileUpdateTest();
        }catch(Exception e){
            Assertions.assertTrue(false); // should not fail
        }

        try {
            ProfileUpdateFailureTest();
            Assertions.assertTrue(false); // should fail
        } catch (Exception e) {
            Assertions.assertEquals("Profile Id is empty", e.getMessage());
        }

        long idValue = Long.MIN_VALUE;
        try {
            ProfileDeleteFailureTest(idValue);
            Assertions.assertTrue(false); // should fail
        } catch (Exception e) {
            Assertions.assertEquals(String.format("No profile found with id '%s'", idValue), e.getMessage());
        }

        try {
            ProfileDeleteFailureTest2();
            Assertions.assertTrue(false); // should fail
        } catch (Exception e) {
            Assertions.assertEquals("Profile Id is empty", e.getMessage());
        }

        try {
            ProfileDeleteTest();
        } catch (Exception e) {
            Assertions.assertTrue(false); // should not fail
        }
    }

    private void clearProfiles() throws InvalidMessageException {
        List<Profile> profileList = irisService.listProfiles();
        for(Profile prof : profileList) {
            irisService.deleteProfile(prof.getId());
        }
    }

    private void ProfileDeleteFailureTest2() throws InvalidMessageException {
        irisService.deleteProfile(-1);
    }

    private void ProfileDeleteFailureTest(long idValue) throws InvalidMessageException  {
        irisService.deleteProfile(idValue);
    }

    private void ProfileDeleteTest() throws InvalidMessageException {
        int initialCount = irisService.listProfiles().size();
        irisService.deleteProfile(testProfile.getId());
        int finalCount = irisService.listProfiles().size();

        Assertions.assertEquals(initialCount, finalCount + 1);
    }

    public void ProfileTest() throws InvalidMessageException {
        testProfile = new Profile();
        String name = "Profile 1";
        String sourceAgency = "srcAgency";
        String originAgency = "orgAgency";
        String destAgency = "desAgency";
        String attention = "attention";

        testProfile.setName(name);
        testProfile.setSourceAgency(sourceAgency);
        testProfile.setIsReportExclude(true);
        testProfile.setOriginatingAgencyIdentifier(originAgency);
        testProfile.setDestinationAgencyIdentifier(destAgency);
        testProfile.setAttentionIndicator(attention);

        irisService.createProfile(testProfile);

        Assertions.assertNotNull(testProfile.getId());
    }

    public void ProfileDuplicationTest() throws InvalidMessageException{
        // Assume ProfileTest() has run and a Profile named "Profile 1" exists in the repository
        Profile profile = new Profile();
        String name = "Profile 1";
        String sourceAgency = "srcAgency";
        String originAgency = "orgAgency";
        String destAgency = "desAgency";
        String attention = "attention";

        profile.setName(name);
        profile.setSourceAgency(sourceAgency);
        profile.setIsReportExclude(true);
        profile.setOriginatingAgencyIdentifier(originAgency);
        profile.setDestinationAgencyIdentifier(destAgency);
        profile.setAttentionIndicator(attention);

        irisService.createProfile(profile);
    }

    public void ProfileListTest() {
        // Assume ProfileTest() ran successfully prior to this method
        List<Profile> profileList = irisService.listProfiles();
        Assertions.assertNotEquals(0, profileList.size());
    }

    public void ProfileUpdateTest() throws InvalidMessageException {
        // Assume ProfileTest() ran successfully prior to this method
        List<Profile> profileList = irisService.listProfiles();

        Profile profile = profileList.get(0);
        String newName = profile.getName() + "Different Name";
        String oldName = profile.getName();
        long id = profile.getId();

        profile.setName(newName);
        irisService.updateProfile(profile);

        List<Profile> newList = irisService.listProfiles();

        Assertions.assertEquals(profileList.size(), newList.size());

        for(Profile prof : newList){
            if(prof.getId() == id){
                Assertions.assertNotEquals(prof.getName(), oldName);
                Assertions.assertEquals(prof.getName(), newName);
            }
        }
    }

    public void ProfileUpdateFailureTest() throws InvalidMessageException {
        Profile profile = new Profile();
        String name = "Profile 37";
        String sourceAgency = "Yep";
        String originAgency = "Nope";
        String destAgency = "Maybe";
        String attention = "Yo!";

        profile.setName(name);
        profile.setSourceAgency(sourceAgency);
        profile.setIsReportExclude(true);
        profile.setOriginatingAgencyIdentifier(originAgency);
        profile.setDestinationAgencyIdentifier(destAgency);
        profile.setAttentionIndicator(attention);

        irisService.updateProfile(profile);
    }
}
