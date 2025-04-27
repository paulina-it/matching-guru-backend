package uk.bovykina.matching_guru.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@RestController
@RequestMapping("/certificates")
public class CertificateController {

    /**
     * Generates a certificate PNG image with the provided participant details.
     */
    @GetMapping(value = "/generate", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateCertificate(
            @RequestParam String name,
            @RequestParam String role,
            @RequestParam String programme,
            @RequestParam String year,
            @RequestParam String date
    ) throws Exception {
        InputStream templateStream = new ClassPathResource("certificates/certificate-template.png").getInputStream();
        BufferedImage image = ImageIO.read(templateStream);

        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setFont(new Font("Brush Script MT", Font.PLAIN, 110));
        g2d.setColor(Color.BLACK);
        drawCenteredText(g2d, name, image.getWidth() / 2, 700);

        g2d.setFont(new Font("Serif", Font.PLAIN, 28));
        drawCenteredText(g2d,
                "has participates in the " + programme + " (" + year + ") programme as a " + role + ".",
                image.getWidth() / 2, 770);

        g2d.setFont(new Font("Serif", Font.PLAIN, 20));
        drawCenteredText(g2d, "Issued on: " + date, image.getWidth() / 2, 870);

        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=certificate.png")
                .contentType(MediaType.IMAGE_PNG)
                .body(baos.toByteArray());
    }

    /**
     * Draws horizontally centred text on the certificate image.
     */
    private void drawCenteredText(Graphics2D g2d, String text, int centerX, int y) {
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        g2d.drawString(text, centerX - textWidth / 2, y);
    }
}
