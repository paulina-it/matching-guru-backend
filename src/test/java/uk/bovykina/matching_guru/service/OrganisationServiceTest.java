package uk.bovykina.matching_guru.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import uk.bovykina.matching_guru.dto.organisation.OrganisationCreateDto;
import uk.bovykina.matching_guru.dto.organisation.OrganisationDto;
import uk.bovykina.matching_guru.dto.organisation.OrganisationUpdateDto;
import uk.bovykina.matching_guru.entity.InviteToken;
import uk.bovykina.matching_guru.entity.Organisation;
import uk.bovykina.matching_guru.entity.User;
import uk.bovykina.matching_guru.mapper.OrganisationMapper;
import uk.bovykina.matching_guru.repository.InviteTokenRepository;
import uk.bovykina.matching_guru.repository.OrganisationRepository;
import uk.bovykina.matching_guru.repository.UserRepository;
import uk.bovykina.matching_guru.util.CloudinaryService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
    private OrganisationMapper organisationMapper;

    @Mock
    private MultipartFile mockFile;

    private Organisation org;
    private OrganisationDto dto;
    private User user;

    @BeforeEach
    void setUp() {
        org = new Organisation();
        org.setId(1L);
        org.setName("Test Org");
        org.setJoinCode("ABC123");

        dto = new OrganisationDto();
        dto.setId(1L);
        dto.setName("Test Org");

        user = new User();
        user.setId(1L);
    }

    @Test
    void testCreateOrganisation_shouldReturnDto() {
        OrganisationCreateDto createDto = new OrganisationCreateDto();
        createDto.setName("Test Org");
        createDto.setDescription("A test organisation");

        when(organisationMapper.toEntity(createDto)).thenReturn(org);
        when(organisationRepository.save(any())).thenReturn(org);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(organisationMapper.toDto(org)).thenReturn(dto);

        OrganisationDto result = organisationService.createOrganisation(createDto, 1L);

        assertEquals("Test Org", result.getName());
        verify(userRepository).save(user);
    }

    @Test
    void testUploadOrganisationLogo_shouldReturnUrl() throws IOException {
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
        when(organisationRepository.findById(1L)).thenReturn(Optional.of(org));
        when(organisationMapper.toDto(org)).thenReturn(dto);

        Optional<OrganisationDto> result = organisationService.getOrganisationById(1L);

        assertTrue(result.isPresent());
        assertEquals("Test Org", result.get().getName());
    }

    @Test
    void testGetAllOrganisations_shouldReturnList() {
        when(organisationRepository.findAll()).thenReturn(List.of(org));
        when(organisationMapper.toDto(org)).thenReturn(dto);

        List<OrganisationDto> result = organisationService.getAllOrganisations();

        assertEquals(1, result.size());
        assertEquals("Test Org", result.get(0).getName());
    }

    @Test
    void testUpdateOrganisation_shouldApplyChanges() {
        OrganisationUpdateDto updateDto = new OrganisationUpdateDto();
        updateDto.setName("New Name");

        OrganisationDto updatedDto = new OrganisationDto();
        updatedDto.setId(1L);
        updatedDto.setName("New Name");

        when(organisationRepository.findById(1L)).thenReturn(Optional.of(org));
        doAnswer(invocation -> {
            org.setName(updateDto.getName());
            return null;
        }).when(organisationMapper).updateFromDto(org, updateDto);
        when(organisationRepository.save(org)).thenReturn(org);
        when(organisationMapper.toDto(org)).thenReturn(updatedDto);

        OrganisationDto result = organisationService.updateOrganisation(1L, updateDto);

        assertEquals("New Name", result.getName());
    }

    @Test
    void testJoinOrganisation_shouldLinkUserToOrganisation() {
        when(organisationRepository.findByJoinCode("ABC123")).thenReturn(Optional.of(org));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(organisationMapper.toDto(org)).thenReturn(dto);

        OrganisationDto result = organisationService.joinOrganisation(1L, "ABC123");

        assertEquals(1L, result.getId());
        verify(userRepository).save(user);
    }
}
