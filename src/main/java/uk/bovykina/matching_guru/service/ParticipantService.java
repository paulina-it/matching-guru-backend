package uk.bovykina.matching_guru.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.bovykina.matching_guru.dto.participant.*;
import uk.bovykina.matching_guru.entity.ParticipantInProgrammeYear;
import uk.bovykina.matching_guru.entity.User;
import uk.bovykina.matching_guru.entity.ProgrammeYear;
import uk.bovykina.matching_guru.repository.ParticipantRepository;
import uk.bovykina.matching_guru.repository.UserRepository;
import uk.bovykina.matching_guru.repository.ProgrammeYearRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final ProgrammeYearRepository programmeYearRepository;

    @Transactional
    public ParticipantResponseDto createParticipant(ParticipantCreateDto createDto) {
        User user = userRepository.findById(createDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        ProgrammeYear programmeYear = programmeYearRepository.findById(createDto.getProgrammeYearId())
                .orElseThrow(() -> new IllegalArgumentException("Programme year not found"));

        ParticipantInProgrammeYear participant = new ParticipantInProgrammeYear();
        participant.setUser(user);
        participant.setProgrammeYear(programmeYear);
        participant.setRole(createDto.getRole());
        participant.setMenteesNumber(createDto.getMenteesNumber());
        participant.setIsMatched(createDto.getIsMatched());
        participant.setAcademicStage(createDto.getAcademicStage());
        participant.setHadPlacement(createDto.getHadPlacement());
        participant.setPlacementDescription(createDto.getPlacementDescription());
        participant.setMotivation(createDto.getMotivation());
        participant.setIsReturningParticipant(createDto.getIsReturningParticipant());

        ParticipantInProgrammeYear savedParticipant = participantRepository.save(participant);
        return toParticipantResponseDto(savedParticipant);
    }

    public ParticipantResponseDto getParticipant(Long id) {
        ParticipantInProgrammeYear participant = participantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found"));
        return toParticipantResponseDto(participant);
    }

    public ParticipantResponseDto getParticipantByUserId(Long id) {
        ParticipantInProgrammeYear participant = participantRepository.findByUserId(id)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found"));
        return toParticipantResponseDto(participant);
    }

    public List<ParticipantResponseDto> getAllParticipants() {
        return participantRepository.findAll().stream()
                .map(this::toParticipantResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ParticipantResponseDto updateParticipant(Long id, ParticipantUpdateDto updateDto) {
        ParticipantInProgrammeYear participant = participantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found"));

        if (updateDto.getRole() != null) {
            participant.setRole(updateDto.getRole());
        }
        if (updateDto.getMenteesNumber() != null) {
            participant.setMenteesNumber(updateDto.getMenteesNumber());
        }
        if (updateDto.getIsMatched() != null) {
            participant.setIsMatched(updateDto.getIsMatched());
        }
        if (updateDto.getAcademicStage() != null) {
            participant.setAcademicStage(updateDto.getAcademicStage());
        }
        if (updateDto.getHadPlacement() != null) {
            participant.setHadPlacement(updateDto.getHadPlacement());
        }
        if (updateDto.getPlacementDescription() != null) {
            participant.setPlacementDescription(updateDto.getPlacementDescription());
        }
        if (updateDto.getMotivation() != null) {
            participant.setMotivation(updateDto.getMotivation());
        }
        if (updateDto.getIsReturningParticipant() != null) {
            participant.setIsReturningParticipant(updateDto.getIsReturningParticipant());
        }

        ParticipantInProgrammeYear updatedParticipant = participantRepository.save(participant);
        return toParticipantResponseDto(updatedParticipant);
    }

    private ParticipantResponseDto toParticipantResponseDto(ParticipantInProgrammeYear participant) {
        ParticipantResponseDto dto = new ParticipantResponseDto();
        dto.setId(participant.getId());

        User user = participant.getUser();
        dto.setUserId(user.getId());
        dto.setUserName(user.getFirstName() + " " + user.getLastName());
        dto.setUserEmail(user.getEmail());

        ProgrammeYear programmeYear = participant.getProgrammeYear();
        dto.setProgrammeYearId(programmeYear.getId());
        dto.setProgrammeName(programmeYear.getProgramme().getName());
        dto.setAcademicYear(programmeYear.getAcademicYear());

        dto.setRole(participant.getRole());
        dto.setMenteesNumber(participant.getMenteesNumber());
        dto.setIsMatched(participant.getIsMatched());
        dto.setAcademicStage(participant.getAcademicStage());
        dto.setHadPlacement(participant.getHadPlacement());
        dto.setPlacementDescription(participant.getPlacementDescription());
        dto.setMotivation(participant.getMotivation());
        dto.setIsReturningParticipant(participant.getIsReturningParticipant());

        return dto;
    }
}