

package it.epicode.u5w3d2pratica.controller;

import it.epicode.u5w3d2pratica.dto.DipendenteDto;
import it.epicode.u5w3d2pratica.exception.NotFoundException;
import it.epicode.u5w3d2pratica.exception.ValidationException;
import it.epicode.u5w3d2pratica.model.Dipendente;
import it.epicode.u5w3d2pratica.service.DipendenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/dipendenti") //
public class DipendenteController {

    @Autowired
    private DipendenteService dipendenteService;


    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<DipendenteDto> Dipendente(@RequestBody DipendenteDto dipendenteDto) {
        try {

            DipendenteDto createdDipendente = dipendenteService.save(dipendenteDto);
            return new ResponseEntity<>(createdDipendente, HttpStatus.CREATED);
        } catch (ValidationException e) {

            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER)")
    public Page<Dipendente> getAllDipendenti(@RequestParam(defaultValue = "0")int page,
                                             @RequestParam(defaultValue = "10")int size,
                                             @RequestParam(defaultValue = "matricola")String sortBy) {


        return dipendenteService.getAllDipendenti(page, size, sortBy);
    }


    @GetMapping("/{id}")

    public ResponseEntity<DipendenteDto> getDipendenteById(@PathVariable Long id) {
        try {

            DipendenteDto dipendente = dipendenteService.get(id);
            return new ResponseEntity<>(dipendente, HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND); // 404 Not Found
        }
    }


    @GetMapping("/page")

    public ResponseEntity<Page<DipendenteDto>> getAllDipendentiPaged(Pageable pageable) {
        Page<DipendenteDto> dipendentiPage = dipendenteService.get(pageable);
        return new ResponseEntity<>(dipendentiPage, HttpStatus.OK);
    }


    @PutMapping("/{id}")

    public ResponseEntity<DipendenteDto> updateDipendente(@PathVariable Long id, @RequestBody DipendenteDto dipendenteDto) {
        try {
            // Come con GET, la logica di autorizzazione dovrebbe verificare i permessi qui.
            DipendenteDto updatedDipendente = dipendenteService.update(id, dipendenteDto);
            return new ResponseEntity<>(updatedDipendente, HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND); // 404 Not Found
        } catch (ValidationException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST); // 400 Bad Request
        }
    }


    @PatchMapping("/{id}/immagine")

    public ResponseEntity<DipendenteDto> uploadImmagineProfilo(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) { // Accepts the image file
        try {
            // Anche in questo caso, la logica di autorizzazione è fondamentale.
            DipendenteDto updatedDipendente = dipendenteService.updateImmagineProfilo(id, file);
            return new ResponseEntity<>(updatedDipendente, HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND); // 404 Not Found
        } catch (Exception e) { // Catches IOException or other upload exceptions
            return new ResponseEntity("Error uploading image: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error
        }
    }



    @DeleteMapping("/{id}")

    public ResponseEntity<Void> deleteDipendente(@PathVariable Long id) {
        try {
            dipendenteService.delete(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content
        } catch (NotFoundException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND); // 404 Not Found
        }
    }
}