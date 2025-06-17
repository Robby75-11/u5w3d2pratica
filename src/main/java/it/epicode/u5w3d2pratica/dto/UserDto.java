package it.epicode.u5w3d2pratica.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class UserDto {
    @NotEmpty(message = "il nome non può essere vuoto")
    private String nome;
    @NotEmpty(message = "il cognome non può essere vuoto")
    private String cognome;
    @NotEmpty(message = "l'email non può essere vuota")
    @Email(message = "l'email deve essere ben strutturata")
    private String email;
    @NotEmpty(message = "la password non può essere vuota")
    private String password;
}