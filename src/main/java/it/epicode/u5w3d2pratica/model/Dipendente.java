package it.epicode.u5w3d2pratica.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data

public class Dipendente {

    @Id
    @GeneratedValue
    private Long id;
    private String username;
    private String nome;
    private String cognome;
    private String email;

    private String immagineProfiloUrl;


}
