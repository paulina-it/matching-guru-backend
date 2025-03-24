package uk.bovykina.matching_guru.util;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(@Value("${cloudinary.cloud-name}") String cloudName,
                             @Value("${cloudinary.api-key}") String apiKey,
                             @Value("${cloudinary.api-secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    /**
     * Uploads an image to Cloudinary and returns the secure URL.
     *
     * @param file   The image file to upload.
     * @param folder The folder in Cloudinary where the image should be stored (e.g., "profile_pictures").
     * @return The URL of the uploaded image.
     * @throws IOException If the upload fails.
     */
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        log.info("📸 Uploading image to Cloudinary...");

        if (file == null || file.isEmpty()) {
            log.error("❌ No file uploaded.");
            throw new IllegalArgumentException("No file uploaded.");
        }

        try {
            Map<String, Object> uploadParams = ObjectUtils.asMap("folder", folder);
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            String imageUrl = uploadResult.get("secure_url").toString();
            log.info("✅ Image uploaded successfully: {}", imageUrl);
            return imageUrl;
        } catch (IOException e) {
            log.error("❌ Cloudinary upload failed: {}", e.getMessage());
            throw new IOException("Error uploading image to Cloudinary", e);
        }
    }

}
