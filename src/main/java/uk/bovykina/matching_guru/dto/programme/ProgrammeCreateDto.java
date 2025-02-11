package uk.bovykina.matching_guru.dto.programme;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class ProgrammeCreateDto {
    @NotBlank(message = "Programme name is required")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Organisation ID is required")
    private Long organisationId;

    @NotNull(message = "Eligible course groups must be specified")
    @Size(min = 1, message = "At least one course group must be selected")
    private Set<Long> courseGroupIds;

    @Email(message = "Contact email must be valid")
    private String contactEmail;
}
