/* 
* NOTICE
*  
* This software (or technical data) was produced for the U. S. Government
* and is subject to the Rights in Data-General Clause 52.227-14, Alt. IV
* (May 2014) – Alternative IV (Dec 2007)
* 
* (c) 2024 The MITRE Corporation. All Rights Reserved.
*/ 

package org.mitre.iwp.common;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.*;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageUtils {

    private static final Logger log = LoggerFactory.getLogger(ImageUtils.class);

    public static BufferedImage read(String cga, int hll, int vll, int bpx, InputStream inputStream) {

        try {
            return read(cga, hll, vll, bpx, IOUtils.toByteArray(inputStream));
        } catch (IOException e) {
            log.error("Could not read input stream", e);
        }

        return null;
    }

    public static BufferedImage read(String cga, int hll, int vll, int bpx, byte[] data) {
      int bytesPerPixel = bpx / 8;

      if(cga.equals("NONE") && (hll * vll == data.length/bytesPerPixel) && bpx == 8){
          log.trace("This is a greyscale image");

          BufferedImage image = new BufferedImage(hll, vll, BufferedImage.TYPE_BYTE_GRAY);
          image.setData(Raster.createRaster(image.getSampleModel(), new DataBufferByte(data, data.length), new Point() ) );

          return image;
      } else if(cga.equals("NONE") && (hll * vll == data.length/bytesPerPixel) && bpx == 24){
          log.trace("This is an RGB image");

          BufferedImage image = new BufferedImage(hll, vll, BufferedImage.TYPE_3BYTE_BGR);
          image.setData(Raster.createRaster(image.getSampleModel(), new DataBufferByte(data, data.length), new Point() ) );
          return image;
      } else if(cga.equals("NONE") && (hll * vll != ( data.length / bytesPerPixel ))){
        log.info("datalength: {} hll: {} vll: {} bpx: {} cga: {}", data.length, hll, vll, bpx, bytesPerPixel);
        return null;
      }

      ByteArrayInputStream bais = new ByteArrayInputStream(data);
      return convertImage(cga, bais);
    }


    /*
        ImageIO recognizes the following formats: BMP, GIF, JPEG, PNG, TIFF, WBMP
        jai-imageio-jpeg2000 plug-in was included to deal with JPEG2000
    */
    private static BufferedImage convertImage(String cga, InputStream is){

        try {
          BufferedImage image = ImageIO.read(is);
          return image;
        } catch (IOException e) {
          log.error("This is not a recognized image type!");
        } catch (Exception e) {
          log.error("Uncaught error: {}", e.getMessage());
        }

        return null;
    }
}

