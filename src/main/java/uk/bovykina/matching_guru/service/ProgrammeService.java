package uk.bovykina.matching_guru.service;

import aj.org.objectweb.asm.commons.Remapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.bovykina.matching_guru.dto.programme.*;
import uk.bovykina.matching_guru.entity.Organisation;
import uk.bovykina.matching_guru.entity.Programme;
import uk.bovykina.matching_guru.repository.OrganisationRepository;
import uk.bovykina.matching_guru.repository.ProgrammeRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ProgrammeService {

    private final ProgrammeRepository programmeRepository;
    private final OrganisationRepository organisationRepository;

    public ProgrammeDto createProgramme(ProgrammeCreateDto createDto) {
        Organisation organisation = organisationRepository.findById(createDto.getOrganisationId())
                .orElseThrow(() -> new IllegalArgumentException("Organisation not found"));

        Programme programme = new Programme();
        programme.setName(createDto.getName());
        programme.setDescription(createDto.getDescription());
        programme.setOrganisation(organisation);

        Programme savedProgramme = programmeRepository.save(programme);
        return toProgrammeDto(savedProgramme);
    }

    public ProgrammeDto updateProgramme(Long id, ProgrammeUpdateDto updateDto) {
        Programme programme = programmeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Programme not found"));

        programme.setName(updateDto.getName());
        programme.setDescription(updateDto.getDescription());

        return toProgrammeDto(programmeRepository.save(programme));
    }

    public void deleteProgramme(Long id) {
        if (!programmeRepository.existsById(id)) {
            throw new IllegalArgumentException("Programme not found");
        }
        programmeRepository.deleteById(id);
    }

    public List<ProgrammeDto> getAllProgrammes() {
        return programmeRepository.findAll()
                .stream()
                .map(this::toProgrammeDto)
                .collect(Collectors.toList());
    }

    public List<ProgrammeDto> getProgrammesByOrganisation(Long organisationId) {
        return programmeRepository.findByOrganisationId(organisationId)
                .stream()
                .map(this::toProgrammeDto)
                .collect(Collectors.toList());
    }


    public ProgrammeDto getProgrammeById(Long id) {
        Programme programme = programmeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Programme not found"));

        return toProgrammeDto(programme);
    }


    private ProgrammeDto toProgrammeDto(Programme programme) {
        ProgrammeDto dto = new ProgrammeDto();
        dto.setId(programme.getId());
        dto.setName(programme.getName());
        dto.setDescription(programme.getDescription());
        dto.setOrganisationId(programme.getOrganisation().getId());
        return dto;
    }

}
