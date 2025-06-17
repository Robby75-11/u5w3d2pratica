package it.epicode.u5w3d2pratica.controller;

import it.epicode.u5w3d2pratica.dto.ViaggioDto;
import it.epicode.u5w3d2pratica.exception.NotFoundException;
import it.epicode.u5w3d2pratica.exception.ValidationException;
import it.epicode.u5w3d2pratica.service.ViaggioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/viaggi")
public class ViaggioController {

    @Autowired
    private ViaggioService viaggioService;

    /**
     * Endpoint per la creazione di un nuovo viaggio.
     * Accessibile agli ADMIN.
     * POST /api/viaggi
     * @param viaggioDto DTO del viaggio da creare.
     * @return ResponseEntity con il DTO del viaggio creato e status 201.
     */
    @PostMapping

    public ResponseEntity<ViaggioDto> createViaggio(@RequestBody ViaggioDto viaggioDto) {
        try {
            ViaggioDto createdViaggio = viaggioService.save(viaggioDto);
            return new ResponseEntity<>(createdViaggio, HttpStatus.CREATED);
        } catch (ValidationException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Endpoint per il recupero di tutti i viaggi.
     * Accessibile a tutti gli utenti autenticati.
     * GET /api/viaggi
     * @return ResponseEntity con la lista di DTO dei viaggi e status 200.
     */
    @GetMapping

    public ResponseEntity<List<ViaggioDto>> getAllViaggi() {
        List<ViaggioDto> viaggi = viaggioService.get();
        return new ResponseEntity<>(viaggi, HttpStatus.OK);
    }

    /**
     * Endpoint per il recupero di un viaggio tramite ID.
     * Accessibile a tutti gli utenti autenticati.
     * GET /api/viaggi/{id}
     * @param id ID del viaggio.
     * @return ResponseEntity con il DTO del viaggio e status 200.
     */
    @GetMapping("/{id}")

    public ResponseEntity<ViaggioDto> getViaggioById(@PathVariable Long id) {
        try {
            ViaggioDto viaggio = viaggioService.get(id);
            return new ResponseEntity<>(viaggio, HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Endpoint per il recupero di tutti i viaggi con paginazione.
     * Accessibile a tutti gli utenti autenticati.
     * GET /api/viaggi/page?page=0&size=10&sort=destinazione,asc
     * @param pageable Oggetto Pageable per la paginazione e l'ordinamento.
     * @return ResponseEntity con una pagina di DTO di viaggi e status 200.
     */
    @GetMapping("/page")

    public ResponseEntity<Page<ViaggioDto>> getAllViaggiPaged(Pageable pageable) {
        Page<ViaggioDto> viaggiPage = viaggioService.get(pageable);
        return new ResponseEntity<>(viaggiPage, HttpStatus.OK);
    }

    /**
     * Endpoint per l'aggiornamento di un viaggio esistente.
     * Accessibile agli ADMIN.
     * PUT /api/viaggi/{id}
     * @param id ID del viaggio da aggiornare.
     * @param viaggioDto DTO con i dati aggiornati del viaggio.
     * @return ResponseEntity con il DTO del viaggio aggiornato e status 200.
     */
    @PutMapping("/{id}")

    public ResponseEntity<ViaggioDto> updateViaggio(@PathVariable Long id, @RequestBody ViaggioDto viaggioDto) {
        try {
            ViaggioDto updatedViaggio = viaggioService.update(id, viaggioDto);
            return new ResponseEntity<>(updatedViaggio, HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (ValidationException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Endpoint per l'eliminazione di un viaggio.
     * Accessibile agli ADMIN.
     * DELETE /api/viaggi/{id}
     * @param id ID del viaggio da eliminare.
     * @return ResponseEntity con status 204 (No Content).
     */
    @DeleteMapping("/{id}")

    public ResponseEntity<Void> deleteViaggio(@PathVariable Long id) {
        try {
            viaggioService.delete(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content
        } catch (NotFoundException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
