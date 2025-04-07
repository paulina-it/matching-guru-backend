package uk.bovykina.matching_guru.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InviteToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    private Long organisationId;

    private String email;

    private LocalDateTime expiryDate;

    public InviteToken(String token, Long organisationId, String email, LocalDateTime expiryDate) {
        this.token = token;
        this.organisationId = organisationId;
        this.email = email;
        this.expiryDate = expiryDate;
    }
}
