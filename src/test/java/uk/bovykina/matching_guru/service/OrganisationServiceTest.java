package uk.bovykina.matching_guru.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import uk.bovykina.matching_guru.dto.organisation.OrganisationCreateDto;
import uk.bovykina.matching_guru.dto.organisation.OrganisationDto;
import uk.bovykina.matching_guru.dto.organisation.OrganisationUpdateDto;
import uk.bovykina.matching_guru.entity.InviteToken;
import uk.bovykina.matching_guru.entity.Organisation;
import uk.bovykina.matching_guru.entity.User;
import uk.bovykina.matching_guru.repository.InviteTokenRepository;
import uk.bovykina.matching_guru.repository.OrganisationRepository;
import uk.bovykina.matching_guru.repository.UserRepository;
import uk.bovykina.matching_guru.util.CloudinaryService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrganisationServiceTest {

    @InjectMocks
    private OrganisationService organisationService;

    @Mock
    private OrganisationRepository organisationRepository;

    @Mock
    private InviteTokenRepository inviteTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private MultipartFile mockFile;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateOrganisation_shouldReturnDto() {
        OrganisationCreateDto createDto = new OrganisationCreateDto();
        createDto.setName("Test Org");
        createDto.setDescription("A test organisation");

        Organisation org = new Organisation();
        org.setId(1L);
        org.setName("Test Org");
        org.setDescription("A test organisation");

        User user = new User();
        user.setId(1L);

        when(organisationRepository.save(any())).thenReturn(org);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        OrganisationDto result = organisationService.createOrganisation(createDto, 1L);

        assertEquals("Test Org", result.getName());
        verify(userRepository).save(user);
    }

    @Test
    void testUploadOrganisationLogo_shouldReturnUrl() throws IOException {
        Organisation org = new Organisation();
        org.setId(1L);

        when(organisationRepository.findById(1L)).thenReturn(Optional.of(org));
        when(cloudinaryService.uploadImage(mockFile, "organisation_logos")).thenReturn("http://image.url");

        String result = organisationService.uploadOrganisationLogo(1L, mockFile);

        assertEquals("http://image.url", result);
        verify(organisationRepository).save(org);
    }

    @Test
    void testDeleteInviteToken_shouldDeleteIfFound() {
        InviteToken token = new InviteToken();
        token.setToken("abc123");
        when(inviteTokenRepository.findByToken("abc123")).thenReturn(Optional.of(token));

        organisationService.deleteInviteToken("abc123");
        verify(inviteTokenRepository).delete(token);
    }

    @Test
    void testGetOrganisationById_shouldReturnDto() {
        Organisation org = new Organisation();
        org.setId(1L);
        org.setName("Test Org");

        when(organisationRepository.findById(1L)).thenReturn(Optional.of(org));

        Optional<OrganisationDto> result = organisationService.getOrganisationById(1L);
        assertTrue(result.isPresent());
        assertEquals("Test Org", result.get().getName());
    }

    @Test
    void testGetAllOrganisations_shouldReturnList() {
        Organisation org = new Organisation();
        org.setName("Test Org");

        when(organisationRepository.findAll()).thenReturn(List.of(org));

        List<OrganisationDto> result = organisationService.getAllOrganisations();
        assertEquals(1, result.size());
    }

    @Test
    void testUpdateOrganisation_shouldApplyChanges() {
        Organisation org = new Organisation();
        org.setId(1L);
        org.setName("Old Name");

        OrganisationUpdateDto dto = new OrganisationUpdateDto();
        dto.setName("New Name");

        when(organisationRepository.findById(1L)).thenReturn(Optional.of(org));
        when(organisationRepository.save(org)).thenReturn(org);

        OrganisationDto result = organisationService.updateOrganisation(1L, dto);

        assertEquals("New Name", result.getName());
    }

    @Test
    void testJoinOrganisation_shouldLinkUserToOrganisation() {
        Organisation org = new Organisation();
        org.setId(1L);
        org.setJoinCode("ABC123");

        User user = new User();
        user.setId(1L);

        when(organisationRepository.findByJoinCode("ABC123")).thenReturn(Optional.of(org));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        OrganisationDto result = organisationService.joinOrganisation(1L, "ABC123");

        assertEquals(1L, result.getId());
        verify(userRepository).save(user);
    }
}
