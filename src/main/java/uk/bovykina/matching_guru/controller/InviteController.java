package uk.bovykina.matching_guru.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.bovykina.matching_guru.entity.InviteToken;
import uk.bovykina.matching_guru.service.InviteService;

@RestController
@RequestMapping("/api/invites")
@RequiredArgsConstructor
public class InviteController {

    private final InviteService inviteTokenService;

    /**
     * Creates a new invite token for a coordinator.
     */
    @PostMapping("/create")
    public ResponseEntity<InviteToken> createInvite(
            @RequestParam Long organisationId,
            @RequestParam String email,
            @RequestParam Long createdByUserId
    ) {
        InviteToken invite = inviteTokenService.createInvite(organisationId, email, createdByUserId);
        return ResponseEntity.ok(invite);
    }

    /**
     * Validates an invite token.
     */
    @GetMapping("/validate")
    public ResponseEntity<InviteToken> validateInvite(@RequestParam String token) {
        InviteToken invite = inviteTokenService.validateToken(token);
        return ResponseEntity.ok(invite);
    }

    /**
     * Marks an invite token as used.
     */
    @PostMapping("/mark-used")
    public ResponseEntity<Void> markUsed(@RequestParam String token) {
        inviteTokenService.markTokenAsUsed(token);
        return ResponseEntity.ok().build();
    }
}
