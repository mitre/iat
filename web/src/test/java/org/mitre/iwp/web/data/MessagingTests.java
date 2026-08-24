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

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mitre.iwp.mismatchrequest.DmeType;
import org.mitre.iwp.mismatchrequest.EncounterType;
import org.mitre.iwp.mismatchrequest.EncountersType;
import org.mitre.iwp.mismatchrequest.IdentityType;
import org.mitre.iwp.mismatchrequest.ImageType;
import org.mitre.iwp.mismatchrequest.ProbeType;
import org.mitre.iwp.mismatchrequest.Request;
import org.mitre.iwp.mismatchrequest.StatusType;
import org.mitre.iwp.mismatchrequest.TargetsType;
import org.mitre.iwp.web.data.config.AbstractInitializer;
import org.mitre.iwp.web.exception.InvalidMessageException;
import org.mitre.iwp.web.model.Candidate;
import org.mitre.iwp.web.model.EbtsMessageRequest;
import org.mitre.iwp.web.model.EyeLabel;
import org.mitre.iwp.web.model.ImageData;
import org.mitre.iwp.web.model.ReportData;
import org.mitre.iwp.web.model.ResponseType;
import org.mitre.iwp.web.service.IrisService;
import org.mitre.jet.ebts.Ebts;
import org.mitre.jet.ebts.EbtsBuilder;
import org.mitre.jet.ebts.EbtsParser;
import org.mitre.jet.ebts.field.Field;
import org.mitre.jet.ebts.field.Occurrence;
import org.mitre.jet.ebts.field.SubField;
import org.mitre.jet.ebts.records.GenericRecord;
import org.mitre.jet.ebts.records.LogicalRecord;
import org.mitre.jet.exceptions.EbtsBuildingException;
import org.mitre.jet.exceptions.EbtsParsingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = { "classpath:application-test.properties",
        "classpath:application.properties" }, properties = { "iwp.security.secured=false",
                "spring.main.allow-circular-references=true" })
@Transactional
public class MessagingTests extends AbstractInitializer {

    @Autowired
    private IrisService irisService;

    public void CreateXmlFile() throws JAXBException, IOException, URISyntaxException {
        byte[] image1 = IOUtils.toByteArray(ClassLoader.getSystemResourceAsStream("Images/Image1.png"));
        byte[] image2 = IOUtils.toByteArray(ClassLoader.getSystemResourceAsStream("Images/Image2.png"));
        byte[] image3 = IOUtils.toByteArray(ClassLoader.getSystemResourceAsStream("Images/Image3.png"));
        byte[] image4 = IOUtils.toByteArray(ClassLoader.getSystemResourceAsStream("Images/Image4.png"));
        byte[] image5 = IOUtils.toByteArray(ClassLoader.getSystemResourceAsStream("Images/Image5.png"));
        byte[] image6 = IOUtils.toByteArray(ClassLoader.getSystemResourceAsStream("Images/Image6.png"));
        byte[] image7 = IOUtils.toByteArray(ClassLoader.getSystemResourceAsStream("Images/Image7.png"));
        byte[] image8 = IOUtils.toByteArray(ClassLoader.getSystemResourceAsStream("Images/Image8.png"));

        Request req = new Request();
        req.setId(BigInteger.valueOf(10));

        ProbeType probe = new ProbeType();
        probe.setUcn("ProbeUcn1");

        EncounterType probeEncounter = new EncounterType();
        probeEncounter.setEcn("ECN17760704ProbeEcn");
        probeEncounter.setEncounterId("ProbeEncounter1");

        ImageType imageType = new ImageType();
        imageType.setValue(image1); // image data
        imageType.setElr(BigInteger.valueOf(11));
        imageType.setDme(DmeType.UC);
        imageType.setStatus(StatusType.ENROLLED);
        probeEncounter.getImages().add(imageType);

        imageType = new ImageType();
        imageType.setValue(image2); // image data
        imageType.setElr(BigInteger.valueOf(11));
        imageType.setDme(DmeType.UC);
        imageType.setStatus(StatusType.ENROLLED);
        probeEncounter.getImages().add(imageType);

        probe.setEncounter(probeEncounter);
        req.setProbe(probe);

        // add candidates
        TargetsType targetsType = new TargetsType();
        targetsType.getTarget().add(CreateCandidate("1", image3, image4));
        targetsType.getTarget().add(CreateCandidate("2", image5, image6));
        targetsType.getTarget().add(CreateCandidate("3", image7, image8));

        req.setTargets(targetsType);

        // write out xml
        JAXBContext jaxbContext = JAXBContext.newInstance(Request.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        URL resourceUrl = ClassLoader.getSystemResource("xmlExampleWithImages.xml");
        File file = new File(resourceUrl.toURI());
        marshaller.marshal(req, file);
    }

    private IdentityType CreateCandidate(String candidateNumber, byte[] image1, byte[] image2) {
        IdentityType candidate = new IdentityType();
        candidate.setUcn(String.format("CandidateUcn%s", candidateNumber));

        EncountersType candidateEncounters = new EncountersType();

        EncounterType encounter = new EncounterType();
        encounter.setEcn(String.format("EcnCandidate%s", candidateNumber));
        encounter.setEncounterId("EID00001");

        ImageType imageType = new ImageType();
        imageType.setValue(image1); // image data
        imageType.setElr(BigInteger.valueOf(11));
        imageType.setDme(DmeType.UC);
        imageType.setStatus(StatusType.ENROLLED);
        encounter.getImages().add(imageType);

        imageType = new ImageType();
        imageType.setValue(image2); // image data
        imageType.setElr(BigInteger.valueOf(22));
        imageType.setDme(DmeType.UC);
        imageType.setStatus(StatusType.ENROLLED);
        encounter.getImages().add(imageType);

        candidateEncounters.getEncounter().add(encounter);
        candidate.setEncounters(candidateEncounters);

        return candidate;
    }

    @Test
    public void TestSrbParsing()
            throws IOException, EbtsParsingException, NoSuchAlgorithmException, EbtsBuildingException {
        byte[] srbFile = createSrbFixture(loadTestImage());
        Ebts ebts = EbtsParser.parse(srbFile);
        LogicalRecord type1 = ebts.getRecordsByType(1).get(0);
        ReportData response = irisService.submitSrb(srbFile, 0l, "");

        Assertions.assertNotNull(response.getProbe());
        Assertions.assertNotEquals(0, response.getCandidates().size());
        Assertions.assertEquals(ResponseType.SRB, response.getResponseType());

        Assertions.assertEquals(response.getProbe().getTransactionControlNumber(), type1.getField(9).toString());
        Assertions.assertEquals(response.getProbe().getTransactionControlReference(), type1.getField(10).toString());

        LogicalRecord type2 = ebts.getRecordsByType(2).get(0);
        Field field = type2.getField(2033);
        List<Candidate> candList = response.getCandidates();

        Assertions.assertEquals(candList.size(), field.getOccurrences().size());

        for (int i = 0; i < candList.size(); i++) {
            Candidate candidate = candList.get(i);
            Occurrence occurrence = field.getOccurrences().get(i);

            Assertions.assertEquals(candidate.getSubjectIdentifier(), occurrence.getSubFields().get(0).toString());
            Assertions.assertEquals(candidate.getScore(), occurrence.getSubFields().get(6).toString());
        }
    }

    @Test
    public void TestEbtsMessageCreation()
            throws IOException, EbtsParsingException, InvalidMessageException, EbtsBuildingException {
        EbtsMessageRequest ebtsReq = new EbtsMessageRequest();

        String dateOfSubmission = "17760704";
        String destAgencyId = "dAgencyId";
        String origAgencyId = "oAgencyid";
        String tcn = "1234-1234-1234";

        String cinPre = "cinPre";
        String cinId = "cinId";
        String attention = "Attention";

        String sourceAgencyId = "sAgencyId";
        String irisCaptureDate = "17760704";
        int numbCandidates = 25;
        EyeLabel elabel = EyeLabel.RIGHT;

        byte[] imageBytes = loadTestImage();

        ebtsReq.setCinPrefix(cinPre);
        ebtsReq.setCinIdentifier(cinId);
        ebtsReq.setDateOfSubmission(dateOfSubmission);
        ebtsReq.setDestinationAgencyIdentifier(destAgencyId);
        ebtsReq.setOriginatingAgencyIdentifier(origAgencyId);
        ebtsReq.setTransactionControlNumber(tcn);
        ebtsReq.setAttentionIndicator(attention);
        ebtsReq.setSourceAgency(sourceAgencyId);
        ebtsReq.setIrisCaptureDate(irisCaptureDate);
        ebtsReq.setNumberOfCandidates(numbCandidates);
        ImageData data = new ImageData();
        data.setEyeLabel(elabel);
        data.setImageBytes(imageBytes);
        data.setImageData(Base64.getEncoder().encodeToString(imageBytes));
        data.setId(1L);
        ebtsReq.setImageData(data);

        Ebts ebts = EbtsParser.parse(irisService.buildEbtsMessage(ebtsReq));

        Assertions.assertEquals(1, ebts.getRecordsByType(1).size());
        LogicalRecord type1 = ebts.getRecordsByType(1).get(0);

        Assertions.assertEquals("EBTS", type1.getField(4).toString());
        Assertions.assertEquals(type1.getField(5).toString(), dateOfSubmission);
        Assertions.assertEquals(type1.getField(7).toString(), destAgencyId);
        Assertions.assertEquals(type1.getField(8).toString(), origAgencyId);
        Assertions.assertEquals(type1.getField(9).toString(), tcn);

        Assertions.assertEquals(1, ebts.getRecordsByType(2).size());
        LogicalRecord type2 = ebts.getRecordsByType(2).get(0);

        Assertions.assertEquals(type2.getField(6).toString(), attention);

        Field field = type2.getField(10);
        Occurrence occurrence = field.getOccurrences().get(0);
        Assertions.assertEquals(occurrence.getSubFields().get(0).toString(), cinPre);
        Assertions.assertEquals(occurrence.getSubFields().get(1).toString(), cinId);

        Assertions.assertEquals(1, ebts.getRecordsByType(17).size());
        LogicalRecord type17 = ebts.getRecordsByType(17).get(0);

        Assertions.assertEquals(type17.getField(4).toString(), sourceAgencyId);
        Assertions.assertEquals(type17.getField(5).toString(), irisCaptureDate);
        Assertions.assertEquals(type17.getField(3).toString(), Integer.toString(elabel.getValue()));

        byte[] ebtsImageBytes = type17.getImageData();

        Assertions.assertArrayEquals(imageBytes, ebtsImageBytes);
    }

    @Test
    public void TestEbtsMessageValidation() throws IOException {
        EbtsMessageRequest ebtsReq = new EbtsMessageRequest();

        ebtsReq.setCinPrefix("cinPre");
        ebtsReq.setCinIdentifier("cinId");
        Assertions.assertFalse(ebtsReq.isValid());

        ebtsReq.setDateOfSubmission("17760704");
        Assertions.assertFalse(ebtsReq.isValid());

        ebtsReq.setDestinationAgencyIdentifier("123456789");
        Assertions.assertFalse(ebtsReq.isValid());

        ebtsReq.setOriginatingAgencyIdentifier("123456789");
        Assertions.assertFalse(ebtsReq.isValid());

        ebtsReq.setTransactionControlNumber("1234567890");
        Assertions.assertFalse(ebtsReq.isValid());

        ebtsReq.setAttentionIndicator("Attention");
        Assertions.assertFalse(ebtsReq.isValid());

        ebtsReq.setSourceAgency("srcAgency");
        Assertions.assertFalse(ebtsReq.isValid());

        ebtsReq.setIrisCaptureDate("17760704");
        Assertions.assertFalse(ebtsReq.isValid());

        ImageData imageData = new ImageData();
        byte[] imageBytes = loadTestImage();
        imageData.setImageBytes(imageBytes);
        imageData.setImageData(Base64.getEncoder().encodeToString(imageBytes));
        imageData.setEyeLabel(EyeLabel.RIGHT);
        ebtsReq.setImageData(imageData);
        Assertions.assertFalse(ebtsReq.isValid());

        ebtsReq.setNumberOfCandidates(20);
        Assertions.assertTrue(ebtsReq.isValid());

        ebtsReq.setNumberOfCandidates(19);
        Assertions.assertFalse(ebtsReq.isValid());

        ebtsReq.setNumberOfCandidates(51);
        Assertions.assertFalse(ebtsReq.isValid());
    }

    private byte[] loadTestImage() throws IOException {
        URL imageUrl = getClass().getClassLoader().getResource("Image.png");
        Assertions.assertNotNull(imageUrl, "Missing test resource Image.png");
        return IOUtils.toByteArray(imageUrl);
    }

    private byte[] createSrbFixture(byte[] imageBytes) throws EbtsBuildingException {
        Ebts ebts = new Ebts();

        LogicalRecord type1 = new GenericRecord(1);
        type1.setField(2, createField("0500"));
        type1.setField(4, createField("SRB"));
        type1.setField(5, createField("17760704"));
        type1.setField(7, createField("SOURCEORI"));
        type1.setField(8, createField("TARGETORI"));
        type1.setField(9, createField("RESPONSE-1234"));
        type1.setField(10, createField("TCN-123-456789"));
        type1.setField(11, createField("00.00"));
        type1.setField(12, createField("00.00"));
        type1.setField(13, createField("NORAM", "EBTS 10.0"));
        type1.setField(16, createField("APO", "APN", "1.0"));
        ebts.addRecord(type1);

        Field candidateList = new Field();
        candidateList.getOccurrences().add(createOccurrence(
                "SUBJECT1", "SMITH,JOHN", "4567", "11", "", "", "95", "40", "", "2", "", "", "", "12"));
        candidateList.getOccurrences().add(createOccurrence(
                "SUBJECT2", "JONES,JIM", "1234", "11", "", "", "75", "40", "", "3", "", "", "", "12"));

        LogicalRecord type2 = new GenericRecord(2);
        type2.setField(6, createField("SMITH,JOHN"));
        type2.setField(79, createField("2"));
        type2.setField(2010, createField("50"));
        type2.setField(2033, candidateList);
        ebts.addRecord(type2);

        ebts.addRecord(createType17Record("1", "SOURCEORI", "17760704", imageBytes));
        ebts.addRecord(createType17Record("2", "SOURCEORI", "17760704", imageBytes));
        ebts.addRecord(createType17Record("3", "OTHERORI", "17760704", imageBytes));

        return new EbtsBuilder().build(ebts);
    }

    private LogicalRecord createType17Record(String idc, String sourceAgency, String captureDate, byte[] imageBytes) {
        LogicalRecord type17 = new GenericRecord(17);
        type17.setField(2, createField(idc));
        type17.setField(3, createField("1"));
        type17.setField(4, createField(sourceAgency));
        type17.setField(5, createField(captureDate));
        type17.setField(6, createField("16"));
        type17.setField(7, createField("16"));
        type17.setField(8, createField("1"));
        type17.setField(9, createField("16"));
        type17.setField(10, createField("16"));
        type17.setField(11, createField("PNG"));
        type17.setField(12, createField("24"));
        type17.setField(13, createField("GRAY"));
        type17.setField(16, createField("0", "0", "0"));
        type17.setField(19, createField("TEST", "MODEL", "SERIAL"));
        type17.setImageData(imageBytes);
        return type17;
    }

    private Field createField(String... subfields) {
        Field field = new Field();
        field.getOccurrences().add(createOccurrence(subfields));
        return field;
    }

    private Occurrence createOccurrence(String... subfields) {
        Occurrence occurrence = new Occurrence();
        for (String subfield : subfields) {
            occurrence.getSubFields().add(new SubField(subfield));
        }
        return occurrence;
    }
}
