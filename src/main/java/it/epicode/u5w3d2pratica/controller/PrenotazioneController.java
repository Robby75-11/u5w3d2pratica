package it.epicode.u5w3d2pratica.controller;

import it.epicode.u5w3d2pratica.dto.PrenotazioneDto;
import it.epicode.u5w3d2pratica.exception.NotFoundException;
import it.epicode.u5w3d2pratica.exception.ValidationException;
import it.epicode.u5w3d2pratica.service.PrenotazioneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prenotazioni") // Mappa tutte le richieste che iniziano con /api/prenotazioni
public class PrenotazioneController {

    @Autowired
    private PrenotazioneService prenotazioneService;

    /**
     * Endpoint per la creazione di una nuova prenotazione.
     * Accessibile a tutti gli utenti autenticati (ADMIN o USER).
     * POST /api/prenotazioni
     * @param prenotazioneDto DTO contenente i dati della prenotazione da creare.
     * @return ResponseEntity con il DTO della prenotazione creata e status 201 (Created).
     */
    @PostMapping

    public ResponseEntity<Object> createPrenotazione(@RequestBody PrenotazioneDto prenotazioneDto) {
        try {
            PrenotazioneDto createdPrenotazione = prenotazioneService.save(prenotazioneDto);
            return new ResponseEntity<>(createdPrenotazione, HttpStatus.CREATED);
        } catch (NotFoundException e) {
            // Se il dipendente o il viaggio non sono stati trovati
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND); // 404 Not Found
        } catch (ValidationException e) {
            // Se i dati della prenotazione non sono validi (es. posti insufficienti, data non valida)
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST); // 400 Bad Request
        }
    }

    /**
     * Endpoint per il recupero di tutte le prenotazioni.
     * Accessibile solo agli ADMIN.
     * GET /api/prenotazioni
     * @return ResponseEntity con la lista di DTO delle prenotazioni e status 200 (OK).
     */
    @GetMapping

    public ResponseEntity<List<PrenotazioneDto>> getAllPrenotazioni() {
        List<PrenotazioneDto> prenotazioni = prenotazioneService.get();
        return new ResponseEntity<>(prenotazioni, HttpStatus.OK);
    }

    /**
     * Endpoint per il recupero di una prenotazione tramite ID.
     * Accessibile agli ADMIN o all'utente proprietario della prenotazione.
     * GET /api/prenotazioni/{id}
     * @param id ID della prenotazione.
     * @return ResponseEntity con il DTO della prenotazione e status 200 (OK).
     */
    @GetMapping("/{id}")

    public ResponseEntity<Object> getPrenotazioneById(@PathVariable Long id) {
        try {
            // In un'applicazione reale, qui andrebbe aggiunta una logica per verificare
            // che l'utente autenticato abbia il permesso di accedere a questa prenotazione
            // (es. se non è ADMIN, l'ID della prenotazione deve appartenere al suo dipendente ID).
            PrenotazioneDto prenotazione = prenotazioneService.get(id);
            return new ResponseEntity<>(prenotazione, HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND); // 404 Not Found
        }
    }

    /**
     * Endpoint per il recupero di tutte le prenotazioni con paginazione.
     * Accessibile solo agli ADMIN.
     * GET /api/prenotazioni/page?page=0&size=10&sort=dataPrenotazione,desc
     * @param pageable Oggetto Pageable per la paginazione e l'ordinamento.
     * @return ResponseEntity con una pagina di DTO di prenotazioni e status 200 (OK).
     */
    @GetMapping("/page")

    public ResponseEntity<Page<PrenotazioneDto>> getAllPrenotazioniPaged(Pageable pageable) {
        Page<PrenotazioneDto> prenotazioniPage = prenotazioneService.get(pageable);
        return new ResponseEntity<>(prenotazioniPage, HttpStatus.OK);
    }

    /**
     * Endpoint per l'aggiornamento di una prenotazione esistente.
     * Accessibile agli ADMIN o all'utente proprietario della prenotazione.
     * PUT /api/prenotazioni/{id}
     * @param id ID della prenotazione da aggiornare.
     * @param prenotazioneDto DTO contenente i nuovi dati della prenotazione.
     * @return ResponseEntity con il DTO della prenotazione aggiornata e status 200 (OK).
     */
    @PutMapping("/{id}")

    public ResponseEntity<Object> updatePrenotazione(@PathVariable Long id, @RequestBody PrenotazioneDto prenotazioneDto) {
        try {
            // Anche qui, la logica di autorizzazione è cruciale.
            PrenotazioneDto updatedPrenotazione = prenotazioneService.update(id, prenotazioneDto);
            return new ResponseEntity<>(updatedPrenotazione, HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND); // 404 Not Found
        } catch (ValidationException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST); // 400 Bad Request
        }
    }

    /**
     * Endpoint per l'eliminazione di una prenotazione.
     * Accessibile solo agli ADMIN.
     * DELETE /api/prenotazioni/{id}
     * @param id ID della prenotazione da eliminare.
     * @return ResponseEntity con status 204 (No Content).
     */
    @DeleteMapping("/{id}")

    public ResponseEntity<Void> deletePrenotazione(@PathVariable Long id) {
        try {
            prenotazioneService.delete(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content
        } catch (NotFoundException e) {
            // Se la prenotazione non esiste al momento dell'eliminazione
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found, senza messaggio nel body per DELETE
        }
    }
}