package uk.bovykina.matching_guru.dto.organisation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganisationCreateDto {
    @NotBlank(message = "Organisation name is required")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Pattern(regexp = "^(http|https)://.*$", message = "Must be a valid URL")
    @Size(max = 2048, message = "URL must not exceed 2048 characters")
    private String logoUrl;
}
