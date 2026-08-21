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

import org.apache.commons.lang3.StringUtils;
import org.mitre.jet.ebts.field.Field;
import org.mitre.jet.ebts.field.Occurrence;
import org.mitre.jet.ebts.field.SubField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class EbtsConversionHelper {
    private static final Logger log = LoggerFactory.getLogger(EbtsConversionHelper.class);
    /**
     *
     * @param field a jet Ebts Field object with desired Integer data
     * @return Returns the integer value input Field parameter. If the Field object is null, method returns 0
     */
    public static int getIntFromField(Field field, Integer defaultVal) {
        if(field != null) {
            try {
                if(StringUtils.isEmpty(field.toString())){
                    return defaultVal;
                }
                
                return Integer.parseInt(field.toString());
            } catch (Exception e) {
                log.warn("Failed to parse field {} as an integer", field);
            }
        }

        return defaultVal;
    }

    /**
     *
     * @param field a jet Ebts Field object with desired String data
     * @return Returns the String value input Field parameter. If the Field object is null, method returns an empty String
     */
    public static String getStringFromField(Field field) {
        if(field != null) {
            try {
                return field.toString();
            } catch (Exception e) {
                log.warn("Failed to get string value from field");
            }
        }

        return "";
    }

    /**
     *
     * @param occurrence a jet Ebts Occurence object with desired String data
     * @param i the index of the desired String data within the Occurence Subfields
     * @return Returns the string value from the ith subfield within the provided Occurrence object.
     * If the location does not contain an ith element, then an empty String is returned
     */
    public static String getStringFromOccurrence(Occurrence occurrence, int i) {
        if(occurrence != null){
            try {
                if(occurrence.getSubFields().size() > i) {
                    return occurrence.getSubFields().get(i).toString();
                }
            }catch(Exception e) {
                log.warn("Failed to get string value from field");
            }
        }

        return "";
    }

    /**
     *
     * @param occurrence a jet Ebts Occurence object with desired Integer data
     * @param i the index of the desired Integer data within the Occurence Subfields
     * @return Returns the Integer value from the ith subfield within the provided Occurrence object. If the location does not contain an Int, or the occurrence subfields do
     * not contain an ith element, then 0 is returned
     */
    public static int getIntFromOccurrence(Occurrence occurrence, int i, Integer defaultVal) {
        if(occurrence != null){
            try {
                if(occurrence.getSubFields().size() > i){
                    String temp = occurrence.getSubFields().get(i).toString();

                    if(StringUtils.isEmpty(temp)){
                        return defaultVal;
                    }

                    return Integer.parseInt(temp);
                }
            }catch(Exception e) {
                log.warn("Failed to parse occurrence {} location {} as an integer", occurrence, i);
            }
        }

        return defaultVal;
    }

    /**
     * @param data String value to be set as data element in the SubField object
     * @return Returns an Ebts SubField object containing the input data string
     */
    public static SubField getSubFieldFromString(String data) {
        SubField field = new SubField();
        field.setData(data);
        return field;
    }

    /**
     * @param data Int value to be set as data element in the SubField object.
     * @return Returns an Ebts SubField object containing the input data string. If input int is Max_Integer,
     * SubField data element will be set to ""
     */
    public static SubField getSubFieldFromInt(int data) {
        SubField field = new SubField();

        if (data == Integer.MAX_VALUE) {
            field.setData("");
        } else {
            field.setData(Integer.toString(data));
        }

        return field;
    }

    /**
     * @param dataArgs array of Strings to be set as data elements in Occurrence SubFields
     * @return Returns an Ebts Occurrence object containing the input data strings.
     */
    public static Occurrence getOccurrenceFromString(String... dataArgs) {
        Occurrence occurrence = new Occurrence();
        List<SubField> subList = new ArrayList<>();
        for (String data : dataArgs) {

            subList.add(getSubFieldFromString(data));
        }

        occurrence.setSubfields(subList);
        return occurrence;
    }

    /**
     * @param dataArgs array of ints to be set as data elements in Occurrence SubFields
     * @return Returns an Ebts Occurrence object containing the input data ints. If input ints are Max_Integer,
     * SubField data element will be set to ""
     */
    public static Occurrence getOccurrenceFromInt(int... dataArgs) {
        Occurrence occurrence = new Occurrence();
        List<SubField> subList = new ArrayList<>();
        for (int data : dataArgs) {

            subList.add(getSubFieldFromInt(data));
        }

        occurrence.setSubfields(subList);
        return occurrence;
    }

    /**
     * @param dataArgs array of Strings to be set as data elements in Field/Occurrence SubFields
     * @return an Ebts Field object with 1 Occurrence and a SubField per dataArg element passed in
     */
    public static Field getFieldFromString(String... dataArgs) {
        Field field = new Field();
        Occurrence occurrence = new Occurrence();
        List<SubField> subList = new ArrayList<>();
        List<Occurrence> occList = new ArrayList<>();

        for (String data : dataArgs) {
            subList.add(getSubFieldFromString(data));
        }

        occurrence.setSubfields(subList);
        occList.add(occurrence);

        field.setOccurrences(occList);

        return field;
    }

    /**
     * @param dataArgs array of Integers to be set as data elements in Field/Occurrence SubFields
     * @return an Ebts Field object with 1 Occurrence and a SubField per dataArg element passed in. If input Integers are Max_Integer,
     * SubField data element will be set to ""
     */
    public static Field getFieldFromInt(Integer... dataArgs) {
        Field field = new Field();
        Occurrence occurrence = new Occurrence();
        List<SubField> subList = new ArrayList<>();
        List<Occurrence> occList = new ArrayList<>();

        for (int data : dataArgs) {
            subList.add(getSubFieldFromInt(data));
        }

        occurrence.setSubfields(subList);
        occList.add(occurrence);

        field.setOccurrences(occList);

        return field;
    }

    /**
     * @return returns and Empty Field object to be populated
     */
    public static Field getEmptyField() {
        Field field = new Field();
        List<Occurrence> occurrenceList = new ArrayList<>();
        field.setOccurrences(occurrenceList);
        return field;
    }

    /**
     *
     * @param occurenceList array of Occurrences
     * @return an Ebts Field object populated with input Occurrences
     */
    public static Field getFieldFromOccurrences(List<Occurrence> occurenceList) {
        Field field = new Field();
        field.setOccurrences(occurenceList);
        return field;
    }
}
