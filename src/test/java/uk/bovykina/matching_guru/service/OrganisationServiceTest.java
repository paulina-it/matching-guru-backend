package uk.bovykina.matching_guru.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.bovykina.matching_guru.entity.InviteToken;
import uk.bovykina.matching_guru.repository.InviteTokenRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrganisationServiceTest {

    @InjectMocks
    private OrganisationService organisationService;

    @Mock
    private InviteTokenRepository inviteTokenRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testValidateInviteToken_Valid() {
        // Arrange
        String token = UUID.randomUUID().toString();
        InviteToken inviteToken = new InviteToken();
        inviteToken.setToken(token);
        inviteToken.setOrganisationId(1L);
        inviteToken.setEmail("test@example.com");
        inviteToken.setExpiryDate(LocalDateTime.now().plusHours(1)); // Valid expiry

        when(inviteTokenRepository.findByToken(token)).thenReturn(Optional.of(inviteToken));

        // Act
        boolean isValid = organisationService.validateInviteToken(token);

        // Assert
        assertTrue(isValid, "The invite token should be valid");
        verify(inviteTokenRepository, never()).delete(inviteToken); // Token should not be deleted
    }

    @Test
    void testValidateInviteToken_Expired() {
        // Arrange
        String token = UUID.randomUUID().toString();
        InviteToken inviteToken = new InviteToken();
        inviteToken.setToken(token);
        inviteToken.setOrganisationId(1L);
        inviteToken.setEmail("test@example.com");
        inviteToken.setExpiryDate(LocalDateTime.now().minusHours(1)); // Expired expiry

        when(inviteTokenRepository.findByToken(token)).thenReturn(Optional.of(inviteToken));

        // Act
        boolean isValid = organisationService.validateInviteToken(token);

        // Assert
        assertFalse(isValid, "The invite token should be invalid (expired)");
        verify(inviteTokenRepository).delete(inviteToken); // Token should be deleted
    }
}
