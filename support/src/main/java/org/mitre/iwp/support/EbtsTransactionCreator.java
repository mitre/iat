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

public class EbtsTransactionCreator {
    public static void main(String[] args) throws IOException, EbtsBuildingException {
        new EbtsTransactionCreator().create();
    }

    public Ebts createDefault() throws IOException {
        Ebts ebts = new Ebts();
        Properties properties = new Properties();
        properties.load(ClassLoader.getSystemResourceAsStream("TransactionCreatorEbts.properties"));

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
        type2.getFields().put(10, toField(properties.getProperty("2.10A"),
                properties.getProperty("2.10B")));
        type2.getFields().put(11, toField(properties.getProperty("2.11")));

        // Type-17
        BufferedImage image = ImageIO.read(ClassLoader.getSystemResourceAsStream(properties.getProperty("17.999")));
        LogicalRecord type17 = new GenericRecord(17);
        type17.getFields().put(3, toField(properties.getProperty("17.3")));
        type17.getFields().put(4, toField(properties.getProperty("17.4")));
        type17.getFields().put(5, toField(properties.getProperty("17.5")));
        type17.getFields().put(6, toField(Integer.toString(image.getWidth())));
        type17.getFields().put(7, toField(Integer.toString(image.getHeight())));
        type17.getFields().put(8, toField(properties.getProperty("17.8")));
        type17.getFields().put(9, toField(Integer.toString(image.getWidth())));
        type17.getFields().put(10, toField(Integer.toString(image.getHeight())));
        type17.getFields().put(11, toField("PNG"));
        type17.getFields().put(12, toField(Integer.toString(24)));
        type17.getFields().put(13, toField("GRAY"));
        type17.getFields().put(19, toField(properties.getProperty("17.19A"),
                properties.getProperty("17.19B"),
                properties.getProperty("17.19C")));

        ByteArrayOutputStream target = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", target);
        type17.setImageData(target.toByteArray());

        ebts.addRecord(type1);
        ebts.addRecord(type2);
        ebts.addRecord(type17);

        return ebts;
    }

    public void create() throws IOException, EbtsBuildingException {
        Ebts ebts = new Ebts();
        Properties properties = new Properties();
        properties.load(ClassLoader.getSystemResourceAsStream("TransactionCreatorEbts.properties"));

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
        type2.getFields().put(10, toField(properties.getProperty("2.10A"),
                properties.getProperty("2.10B")));
        type2.getFields().put(11, toField(properties.getProperty("2.11")));

        // Type-17
        BufferedImage image = ImageIO.read(ClassLoader.getSystemResourceAsStream(properties.getProperty("17.999")));
        LogicalRecord type17 = new GenericRecord(17);
        type17.getFields().put(3, toField(properties.getProperty("17.3")));
        type17.getFields().put(4, toField(properties.getProperty("17.4")));
        type17.getFields().put(5, toField(properties.getProperty("17.5")));
        type17.getFields().put(6, toField(Integer.toString(image.getWidth())));
        type17.getFields().put(7, toField(Integer.toString(image.getHeight())));
        type17.getFields().put(8, toField(properties.getProperty("17.8")));
        type17.getFields().put(9, toField(Integer.toString(image.getWidth())));
        type17.getFields().put(10, toField(Integer.toString(image.getHeight())));
        type17.getFields().put(11, toField("PNG"));
        type17.getFields().put(12, toField(Integer.toString(24)));
        type17.getFields().put(13, toField("GRAY"));
        type17.getFields().put(19, toField(properties.getProperty("17.19A"),
                properties.getProperty("17.19B"),
                properties.getProperty("17.19C")));

        ByteArrayOutputStream target = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", target);
        type17.setImageData(target.toByteArray());

        ebts.addRecord(type1);
        ebts.addRecord(type2);
        ebts.addRecord(type17);

        Files.write(new EbtsBuilder().build(ebts), new File("ebts.eft"));
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
}
