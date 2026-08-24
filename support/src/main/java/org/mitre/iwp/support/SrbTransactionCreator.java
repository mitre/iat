/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

package org.mitre.iwp.support;

import com.google.common.io.Files;
import org.mitre.jet.ebts.Ebts;
import org.mitre.jet.ebts.EbtsBuilder;
import org.mitre.jet.ebts.field.Field;
import org.mitre.jet.ebts.field.Occurrence;
import org.mitre.jet.ebts.field.SubField;
import org.mitre.jet.ebts.records.GenericRecord;
import org.mitre.jet.ebts.records.LogicalRecord;
import org.mitre.jet.exceptions.EbtsBuildingException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class SrbTransactionCreator {
    public static void main(String[] args) throws IOException, EbtsBuildingException {
        new SrbTransactionCreator().create();
    }

    public void create() throws IOException, EbtsBuildingException {
        Ebts ebts = new Ebts();
        Properties properties = new Properties();
        properties.load(ClassLoader.getSystemResourceAsStream("TransactionCreatorSrb.properties"));

        // Type-1
        LogicalRecord type1 = new GenericRecord(1);
        type1.getFields().put(2, toField(properties.getProperty("1.2")));
        type1.getFields().put(4, toField(properties.getProperty("1.4")));
        type1.getFields().put(5, toField(properties.getProperty("1.5")));
        type1.getFields().put(7, toField(properties.getProperty("1.7")));
        type1.getFields().put(8, toField(properties.getProperty("1.8")));
        type1.getFields().put(9, toField(properties.getProperty("1.9")));
        type1.getFields().put(10, toField(properties.getProperty("1.10")));
        type1.getFields().put(11, toField(properties.getProperty("1.11")));
        type1.getFields().put(12, toField(properties.getProperty("1.12")));
        type1.getFields().put(13, toField(properties.getProperty("1.13A"),
                properties.getProperty("1.13B")));
        type1.getFields().put(16, toField(properties.getProperty("1.16A"),
                properties.getProperty("1.16B"),
                properties.getProperty("1.16C")));

        // Type-2
        LogicalRecord type2 = new GenericRecord(2);
        type2.getFields().put(6, toField(properties.getProperty("2.6")));
        type2.getFields().put(79, toField(properties.getProperty("2.79")));
        type2.getFields().put(50, toField(properties.getProperty("2.2010")));

        Field f2033 = new Field();
        for(int i = 1; i <= 2; i++) {
            String[] occurrence = new String['N'-'A'+1];
            for(char c = 'A'; c <= 'N'; c++) {
                occurrence[('N' - 'A') - ('N' - c)] = properties.getProperty(String.format("2.2033_%d%s", i, c));
            }
            f2033.getOccurrences().add(toOccurrence(occurrence));
        }
        type2.getFields().put(2033, f2033);

        ebts.addRecord(type1);
        ebts.addRecord(type2);

        for(int i = 1; i <= 3; i++) {
            // Type-17
            BufferedImage image = ImageIO.read(ClassLoader.getSystemResourceAsStream(properties.getProperty("17.999_"+i)));
            LogicalRecord type17 = new GenericRecord(17);
            type17.getFields().put(2, toField(properties.getProperty("17.2_"+i)));
            type17.getFields().put(3, toField(properties.getProperty("17.3_"+i)));
            type17.getFields().put(4, toField(properties.getProperty("17.4_"+i)));
            type17.getFields().put(5, toField(properties.getProperty("17.5_"+i)));
            type17.getFields().put(6, toField(Integer.toString(image.getWidth())));
            type17.getFields().put(7, toField(Integer.toString(image.getHeight())));
            type17.getFields().put(8, toField(properties.getProperty("17.8_"+i)));
            type17.getFields().put(9, toField(Integer.toString(image.getWidth())));
            type17.getFields().put(10, toField(Integer.toString(image.getHeight())));
            type17.getFields().put(11, toField("PNG"));
            type17.getFields().put(12, toField(Integer.toString(24)));
            type17.getFields().put(13, toField("GRAY"));
            type17.getFields().put(19, toField(properties.getProperty("17.19_"+i+"A"),
                    properties.getProperty("17.19_"+i+"B"),
                    properties.getProperty("17.19_"+i+"C")));

            ByteArrayOutputStream target = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", target);
            type17.setImageData(target.toByteArray());
            ebts.addRecord(type17);
        }

        Files.write(new EbtsBuilder().build(ebts), new File("srb.eft"));
    }

    private static Field toField(String... subfields) {
        Field f = new Field();
        Occurrence occurrence = new Occurrence();
        for(int i = 0; i < subfields.length; i++) {
            occurrence.getSubFields().add(new SubField(subfields[i]));
        }
        f.getOccurrences().add(occurrence);
        return f;
    }

    private static Occurrence toOccurrence(String... subfields) {
        Occurrence occurrence = new Occurrence();
        for(int i = 0; i < subfields.length; i++) {
            occurrence.getSubFields().add(new SubField(subfields[i]));
        }
        return occurrence;
    }
}
