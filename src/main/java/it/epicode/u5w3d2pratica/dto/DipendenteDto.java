package it.epicode.u5w3d2pratica.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
    public class DipendenteDto {

    private Long id;
    @NotEmpty(message = "il campo username non può essere vuoto")
    private String username;
    @NotEmpty(message = "il nome non può essere vuoto")
    private String nome;
    @NotEmpty(message = "il cognome non può essere vuoto")
    private String cognome;
    @Email(message = "l'email deve avere un formato valido")
    @NotBlank(message = "l'email non può essere vuota")
    private String email;
    // Includo l'URL anche qui per la risposta, ma non sarà modificabile direttamente tramite questo DTO
    // L'upload dell'immagine avrà un endpoint separato.
    private String immagineProfiloUrl;


}
