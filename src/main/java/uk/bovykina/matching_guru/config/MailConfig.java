package uk.bovykina.matching_guru.config;

import com.sendgrid.SendGrid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailConfig {

    @Bean
    public SendGrid sendGrid(@Value("${sendgrid.api-key}") String key) {
        return new SendGrid(key);
    }
}
