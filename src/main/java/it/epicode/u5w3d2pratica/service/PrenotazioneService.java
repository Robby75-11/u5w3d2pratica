package it.epicode.u5w3d2pratica.service;

import it.epicode.u5w3d2pratica.dto.PrenotazioneDto;
import it.epicode.u5w3d2pratica.exception.NotFoundException;
import it.epicode.u5w3d2pratica.exception.ValidationException;
import it.epicode.u5w3d2pratica.model.Dipendente;
import it.epicode.u5w3d2pratica.model.Prenotazione;
import it.epicode.u5w3d2pratica.model.Viaggio;
import it.epicode.u5w3d2pratica.repository.DipendenteRepository;
import it.epicode.u5w3d2pratica.repository.PrenotazioneRepository;
import it.epicode.u5w3d2pratica.repository.ViaggioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrenotazioneService {

    @Autowired
    private PrenotazioneRepository prenotazioneRepository;

    @Autowired
    private DipendenteRepository dipendenteRepository; // Per trovare il dipendente associato
    @Autowired
    private ViaggioRepository viaggioRepository;       // Per trovare il viaggio associato

    // --- Metodi Helper di Mappatura ---

    // Mappa un'entità Prenotazione a un DTO PrenotazioneDto
    private PrenotazioneDto mapToPrenotazioneDto(Prenotazione prenotazione) {
        PrenotazioneDto dto = new PrenotazioneDto();
        dto.setId(prenotazione.getId());
        dto.setDataPrenotazione(prenotazione.getDataPrenotazione()); // Corrisponde al campo nel DTO
        dto.setNumeroPosti(prenotazione.getNumeroPosti());
        dto.setNotePreferenze(prenotazione.getNotePreferenze());

        // Associa gli ID delle entità correlate al DTO
        if (prenotazione.getDipendente() != null) {
            dto.setDipendenteId(prenotazione.getDipendente().getId());
        }
        if (prenotazione.getViaggio() != null) {
            dto.setIdViaggio(prenotazione.getViaggio().getId());
        }
        return dto;
    }

    // Mappa un DTO PrenotazioneDto a un'entità Prenotazione
    // (Questo metodo NON imposta ID, Viaggio o Dipendente, ma solo i campi diretti del DTO)
    private Prenotazione mapToPrenotazioneEntity(PrenotazioneDto dto, Prenotazione prenotazione) {
        prenotazione.setDataPrenotazione(dto.getDataPrenotazione()); // Mappa la data della prenotazione
        prenotazione.setNumeroPosti(dto.getNumeroPosti());
        prenotazione.setNotePreferenze(dto.getNotePreferenze());
        return prenotazione;
    }

    // --- Operazioni CRUD ---

    /**
     * Crea una nuova prenotazione nel sistema.
     * Controlla la validità dei dati, la disponibilità dei posti e associa le entità correlate.
     *
     * @param prenotazioneDto DTO contenente i dati della prenotazione da salvare.
     * @return Il DTO della prenotazione appena salvata.
     * @throws ValidationException se i dati della prenotazione non sono validi o non ci sono posti.
     * @throws NotFoundException se il dipendente o il viaggio specificati non esistono.
     */

    public PrenotazioneDto save(PrenotazioneDto prenotazioneDto) throws ValidationException, NotFoundException {
        // Validazioni iniziali sui dati del DTO
        if (prenotazioneDto.getDataPrenotazione().isAfter(LocalDate.now())) {
            throw new ValidationException("La data della prenotazione non può essere nel futuro.");
        }
        if (prenotazioneDto.getNumeroPosti() <= 0) {
            throw new ValidationException("Il numero di posti deve essere almeno 1.");
        }

        // Recupera le entità Dipendente e Viaggio usando gli ID dal DTO
        Dipendente dipendente = dipendenteRepository.findById(prenotazioneDto.getDipendenteId())
                .orElseThrow(() -> new NotFoundException("Dipendente con ID " + prenotazioneDto.getDipendenteId() + " non trovato."));

        Viaggio viaggio = viaggioRepository.findById(prenotazioneDto.getIdViaggio())
                .orElseThrow(() -> new NotFoundException("Viaggio con ID " + prenotazioneDto.getIdViaggio() + " non trovato."));

        // Verifica la disponibilità dei posti nel viaggio
        if (viaggio.getPostiDisponibili() < prenotazioneDto.getNumeroPosti()) {
            throw new ValidationException("Non ci sono abbastanza posti disponibili per il viaggio selezionato. Posti rimasti: " + viaggio.getPostiDisponibili());
        }

        // Crea la nuova entità Prenotazione e mappa i campi dal DTO
        Prenotazione prenotazione = new Prenotazione();
        prenotazione = mapToPrenotazioneEntity(prenotazioneDto, prenotazione);
        prenotazione.setDipendente(dipendente); // Associa il dipendente
        prenotazione.setViaggio(viaggio);       // Associa il viaggio
        // L'entità potrebbe avere anche `dataRichiesta` se diversa da `dataPrenotazione`,
        // ma la logica qui è che `dataPrenotazione` è la data dell'effettiva prenotazione.
        // Se `dataRichiesta` nell'entity è una data diversa, dovrai popolarla qui.
        // Per ora, assumo che `dataPrenotazione` nel DTO si mappi a `dataPrenotazione` nell'entity.
        // Se `dataRichiesta` nell'entity è la data di creazione della prenotazione, puoi impostarla:
        // prenotazione.setDataRichiesta(LocalDate.now()); // Esempio se fosse la data di creazione


        // Aggiorna il numero di posti disponibili nel viaggio
        viaggio.setPostiDisponibili(viaggio.getPostiDisponibili() - prenotazioneDto.getNumeroPosti());
        viaggioRepository.save(viaggio); // Salva il viaggio con i posti aggiornati

        // Salva la prenotazione nel database
        Prenotazione savedPrenotazione = prenotazioneRepository.save(prenotazione);
        return mapToPrenotazioneDto(savedPrenotazione); // Restituisce il DTO della prenotazione salvata
    }

    /**
     * Recupera tutte le prenotazioni esistenti.
     *
     * @return Una lista di DTO di tutte le prenotazioni.
     */

    public List<PrenotazioneDto> get() {
        return prenotazioneRepository.findAll().stream()
                .map(this::mapToPrenotazioneDto)
                .collect(Collectors.toList());
    }

    /**
     * Recupera una singola prenotazione tramite il suo ID.
     *
     * @param id ID della prenotazione da recuperare.
     * @return Il DTO della prenotazione trovata.
     * @throws NotFoundException se la prenotazione con l'ID specificato non esiste.
     */

    public PrenotazioneDto get(Long id) throws NotFoundException {
        Prenotazione prenotazione = prenotazioneRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Prenotazione con ID " + id + " non trovata"));
        return mapToPrenotazioneDto(prenotazione);
    }

    /**
     * Recupera tutte le prenotazioni con paginazione.
     *
     * @param pageable Oggetto Pageable per la paginazione e l'ordinamento.
     * @return Una pagina di DTO di prenotazioni.
     */

    public Page<PrenotazioneDto> get(Pageable pageable) {
        return prenotazioneRepository.findAll(pageable)
                .map(this::mapToPrenotazioneDto);
    }

    /**
     * Aggiorna una prenotazione esistente.
     * Gestisce il cambiamento del numero di posti o del viaggio associato.
     *
     * @param id ID della prenotazione da aggiornare.
     * @param prenotazioneDto DTO contenente i nuovi dati della prenotazione.
     * @return Il DTO della prenotazione aggiornata.
     * @throws NotFoundException se la prenotazione, il dipendente o il viaggio non esistono.
     * @throws ValidationException se i dati non sono validi o non ci sono abbastanza posti.
     */

    public PrenotazioneDto update(Long id, PrenotazioneDto prenotazioneDto) throws NotFoundException, ValidationException {
        // Recupera la prenotazione esistente
        Prenotazione existingPrenotazione = prenotazioneRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Prenotazione con ID " + id + " non trovata"));

        // Salva il vecchio numero di posti e il vecchio viaggio prima delle modifiche
        int oldNumeroPosti = existingPrenotazione.getNumeroPosti();
        Viaggio oldViaggio = existingPrenotazione.getViaggio();

        // Recupera il nuovo dipendente e il nuovo viaggio (potrebbero essere gli stessi)
        Dipendente newDipendente = dipendenteRepository.findById(prenotazioneDto.getDipendenteId())
                .orElseThrow(() -> new NotFoundException("Dipendente con ID " + prenotazioneDto.getDipendenteId() + " non trovato."));

        Viaggio newViaggio = viaggioRepository.findById(prenotazioneDto.getIdViaggio())
                .orElseThrow(() -> new NotFoundException("Viaggio con ID " + prenotazioneDto.getIdViaggio() + " non trovato."));

        // Validazioni sui dati del DTO
        if (prenotazioneDto.getDataPrenotazione().isAfter(LocalDate.now())) {
            throw new ValidationException("La data della prenotazione non può essere nel futuro.");
        }
        if (prenotazioneDto.getNumeroPosti() <= 0) {
            throw new ValidationException("Il numero di posti deve essere almeno 1.");
        }

        // Logica per aggiornare i posti disponibili se il viaggio cambia o il numero di posti cambia
        if (!oldViaggio.getId().equals(newViaggio.getId())) {
            // Il viaggio è cambiato: ripristina i posti nel vecchio viaggio e sottrai dal nuovo
            oldViaggio.setPostiDisponibili(oldViaggio.getPostiDisponibili() + oldNumeroPosti);
            viaggioRepository.save(oldViaggio); // Salva il ripristino per il vecchio viaggio

            if (newViaggio.getPostiDisponibili() < prenotazioneDto.getNumeroPosti()) {
                // Non ci sono posti sufficienti nel nuovo viaggio per la prenotazione
                throw new ValidationException("Non ci sono abbastanza posti disponibili nel nuovo viaggio. Posti rimasti: " + newViaggio.getPostiDisponibili());
            }
            newViaggio.setPostiDisponibili(newViaggio.getPostiDisponibili() - prenotazioneDto.getNumeroPosti());

        } else {
            // Il viaggio è lo stesso, gestisci solo la variazione del numero di posti
            int deltaPosti = prenotazioneDto.getNumeroPosti() - oldNumeroPosti;
            if (newViaggio.getPostiDisponibili() < deltaPosti) {
                throw new ValidationException("Non ci sono abbastanza posti disponibili per questa modifica. Posti rimasti: " + newViaggio.getPostiDisponibili());
            }
            newViaggio.setPostiDisponibili(newViaggio.getPostiDisponibili() - deltaPosti);
        }

        viaggioRepository.save(newViaggio); // Salva l'aggiornamento dei posti nel nuovo/stesso viaggio

        // Aggiorna l'entità Prenotazione con i nuovi dati dal DTO
        existingPrenotazione = mapToPrenotazioneEntity(prenotazioneDto, existingPrenotazione);
        existingPrenotazione.setDipendente(newDipendente); // Associa il nuovo dipendente
        existingPrenotazione.setViaggio(newViaggio);       // Associa il nuovo viaggio

        Prenotazione updatedPrenotazione = prenotazioneRepository.save(existingPrenotazione);
        return mapToPrenotazioneDto(updatedPrenotazione);
    }

    /**
     * Elimina una prenotazione esistente e ripristina i posti nel viaggio associato.
     *
     * @param id ID della prenotazione da eliminare.
     * @throws NotFoundException se la prenotazione non esiste.
     */

    public void delete(Long id) throws NotFoundException {
        Prenotazione prenotazione = prenotazioneRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Prenotazione con ID " + id + " non trovata"));

        // Ripristina i posti disponibili nel viaggio
        Viaggio viaggio = prenotazione.getViaggio();
        if (viaggio != null) {
            viaggio.setPostiDisponibili(viaggio.getPostiDisponibili() + prenotazione.getNumeroPosti());
            viaggioRepository.save(viaggio); // Salva il viaggio con i posti ripristinati
        }

        prenotazioneRepository.deleteById(id);
    }
}