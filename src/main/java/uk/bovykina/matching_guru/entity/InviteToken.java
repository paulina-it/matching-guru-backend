package uk.bovykina.matching_guru.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InviteToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    private String token;

    @NonNull
    private Long organisationId;

    @NonNull
    private String email;

    @NonNull
    private LocalDateTime expiryDate;

    @Builder.Default
    private boolean used = false;

    @NonNull
    private Long createdByUserId;

    public boolean isExpired() {
        return expiryDate.isBefore(LocalDateTime.now());
    }
}
