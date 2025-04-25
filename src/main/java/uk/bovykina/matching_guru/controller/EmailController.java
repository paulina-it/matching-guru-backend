package uk.bovykina.matching_guru.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.bovykina.matching_guru.service.EmailService;

@RestController
@RequestMapping("/test-mail")
@RequiredArgsConstructor
public class EmailController {
    private final EmailService emailService;

    @GetMapping("/{address}")
    public ResponseEntity<String> ping(@PathVariable String address) {
        emailService.sendAsync(
                address,
                "Matching-Guru says hello 👋",
                "<p>If you received this, mailing works!</p>");
        return ResponseEntity.ok("queued");
    }
}
