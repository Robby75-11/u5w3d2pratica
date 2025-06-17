package it.epicode.u5w3d2pratica.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PrenotazioneDto {

    private Long id; //sarà nullo in Post, valorizzato in GET

    @NotNull(message = "L'ID del dipendente non può essere nullo")
    private Long dipendenteId;

    @Min(value = 2, message = "Il numero di posti deve essere almeno 2")
    private int numeroPosti;

    @NotNull(message = "L'id del viaggio non può essere nullo")
    private Long idViaggio;

       // La dataRichiesta sarà la data in cui viene creata la prenotazione,
    // può essere generata dal backend o inviata (ma è più comune generarla)
    @NotNull(message = "La data della prenotazione è obbligatoria")
    private LocalDate dataPrenotazione; // Può essere generata dal service

    private String notePreferenze;

}
