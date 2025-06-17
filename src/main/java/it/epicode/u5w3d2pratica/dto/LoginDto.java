package it.epicode.u5w3d2pratica.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class LoginDto {

    @NotEmpty(message = "l'email non può essere vuota")
    @Email(message = "l'email deve essere ben strutturata")
    private String email;
    @NotEmpty(message = "la password non può essere vuota")
    private String password;
}
