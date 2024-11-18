package uk.bovykina.matching_guru.dto.user;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginDto {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}