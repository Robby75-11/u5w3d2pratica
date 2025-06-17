package it.epicode.u5w3d2pratica.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@Table(
        name = "prenotazioni",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"dipendente_id", "data_prenotazione"})
        }
)
public class Prenotazione {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "viaggio_id")
    private Viaggio viaggio;

    @ManyToOne
    @JoinColumn(name = "dipendente_id")
    private Dipendente dipendente;

    @Column(name = "data_richiesta", nullable = false)
    private LocalDate dataRichiesta;

    @Column(columnDefinition = "TEXT")
    private String notePreferenze;

    @Column(name = "data_prenotazione", nullable = false)
    private LocalDate dataPrenotazione;

    private int numeroPosti;


}
