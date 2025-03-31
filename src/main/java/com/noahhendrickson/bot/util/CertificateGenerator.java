package com.noahhendrickson.bot.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

public class CertificateGenerator {

    private static final Logger logger = LoggerFactory.getLogger(CertificateGenerator.class);
    private static final String TEMPLATE_PATH = "/images/Beach_Bunny_Diploma_2025_BLANK.png";
    private static final String FONT_PATH = "/fonts/OPTIEngraversOldEnglish.otf";

    public static File generateCertificate(String username) throws Exception {
        logger.info("Generating certificate for user: {}", username);

        try (InputStream templateStream = CertificateGenerator.class.getResourceAsStream(TEMPLATE_PATH);
             InputStream fontStream = CertificateGenerator.class.getResourceAsStream(FONT_PATH)) {

            if (templateStream == null) throw new IllegalStateException("Template image not found.");
            if (fontStream == null) throw new IllegalStateException("Font file not found.");

            BufferedImage template = ImageIO.read(templateStream);
            Graphics2D g2d = template.createGraphics();

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            Font nameFont = baseFont.deriveFont(Font.PLAIN, 100f);

            g2d.setFont(nameFont);
            g2d.setColor(Color.decode("#e5b5cd")); // Pink

            FontRenderContext frc = g2d.getFontRenderContext();
            Rectangle bounds = nameFont.getStringBounds(username, frc).getBounds();
            int x = (template.getWidth() - bounds.width) / 2;
            int y = 900;

            g2d.drawString(username, x, y);
            g2d.dispose();

            File output = new File("certificates/" + username.replaceAll("\\s+", "_") + "_certificate.png");
            if (output.getParentFile().mkdirs()) {
                logger.debug("Created directories for certificate output.");
            }

            ImageIO.write(template, "png", output);
            logger.info("Certificate created successfully at {}", output.getAbsolutePath());

            return output;

        } catch (Exception e) {
            logger.error("Failed to generate certificate for user: {}", username, e);
            throw e;
        }
    }
}
