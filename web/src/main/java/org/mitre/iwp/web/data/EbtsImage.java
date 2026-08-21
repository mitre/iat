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

import org.mitre.iwp.common.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

@Entity
public class EbtsImage {
    private static final Logger logger = LoggerFactory.getLogger(EbtsImage.class);
    @Id
    @GeneratedValue
    private Long id;
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    private String originalImageType;
    public String getOriginalImageType() { return this.originalImageType; }
    public void setOriginalImageType(String type) { this.originalImageType = type; }

    @Column(name = "image_hash", unique = true, nullable = false)
    private String imageHash;
    public String getImageHash() { return this.imageHash; }
    public void setImageHash( String hash ) { this.imageHash = hash; }

    @Lob
	@Column(columnDefinition="LONGBLOB")
    private byte[] tshepiiResponse;
    public byte[] getTshepiiResponse() { return this.tshepiiResponse; }
    public void setTshepiiResponse(byte[] response){ this.tshepiiResponse = response; }

    private String contactClassifierResponse;
    public String getContactClassifierResponse() { return this.contactClassifierResponse; }
    public void setContactClassifierResponse(String response) { this.contactClassifierResponse = response; }

    private String path;
    public String getPath() { return path; } 
    public void setPath( String p ) { this.path = p; }

    public EbtsImage() {
      // Empty constructor to satisfy JPA requirement
  }

    public String toString() {
        return String.format("%s#<ebtsImageFormat='%s', path='%s'>",
                this.getClass().getSimpleName(),
                this.originalImageType,
                this.path);
    }

    public static byte[] buildThumbnail(EbtsImage image) throws IOException {
      ByteArrayInputStream bais = new ByteArrayInputStream(EbtsImage.getImageBytesPng(image));
      BufferedImage sourceImage = ImageIO.read(bais);
      AffineTransform tx = new AffineTransform();
      tx.scale(.25, .25);

      AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
      sourceImage = op.filter(sourceImage, null);

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ImageIO.write(sourceImage, "png", out);
      return out.toByteArray();
    }

    public static byte[] getImageBytes(EbtsImage image) throws IOException {
        try (FileInputStream fis = new FileInputStream(image.getPath())) {
            byte[] buffer = new byte[fis.available()];
            if(fis.read(buffer) <= 0) {
                throw new IOException(String.format("Unable to read file %s", image.getPath()));
            }

            return buffer;
        }
    }

    public static byte[] getImageBytesPng(EbtsImage image) throws IOException {
      String contentType = image.getOriginalImageType();
      byte[] imageData = EbtsImage.getImageBytes(image);

      if(contentType.equals("image/png")) { 
          return imageData;
      }

      contentType = contentType.replace("image/", "").toUpperCase();
      BufferedImage img = ImageUtils.read(contentType, 16, 16, 8, imageData);

      if(img == null) {
        throw new IOException(String.format("Unsupported Image Format (%s)", contentType));
      }

      try {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(img, "png", outputStream);

        return outputStream.toByteArray();
      } catch(Exception e) {
        logger.info(e.getMessage());
      }

      return new byte[0];
    }
}
