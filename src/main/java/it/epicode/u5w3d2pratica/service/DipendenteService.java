package it.epicode.u5w3d2pratica.service;

import com.cloudinary.Cloudinary;
import it.epicode.u5w3d2pratica.dto.DipendenteDto;
import it.epicode.u5w3d2pratica.exception.NotFoundException;
import it.epicode.u5w3d2pratica.exception.ValidationException;
import it.epicode.u5w3d2pratica.model.Dipendente;
import it.epicode.u5w3d2pratica.repository.DipendenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class DipendenteService {

    @Autowired
    private DipendenteRepository dipendenteRepository;

    @Autowired(required = false) // 'required = false' se Cloudinary non è sempre configurato (es. in test)
    private Cloudinary cloudinary;

    @Autowired
    private ViaggioService viaggioService;

     @Autowired
     private JavaMailSender javaMailSender;

    // --- Metodi Helper ---
    private DipendenteDto mapToDipendenteDto(Dipendente dipendente) {
        DipendenteDto dto = new DipendenteDto();
        dto.setId(dipendente.getId());
        dto.setUsername(dipendente.getUsername());
        dto.setNome(dipendente.getNome());
        dto.setCognome(dipendente.getCognome());
        dto.setEmail(dipendente.getEmail());
        dto.setImmagineProfiloUrl(dipendente.getImmagineProfiloUrl());
        return dto;
    }

    private Dipendente mapToDipendenteEntity(DipendenteDto dto, Dipendente dipendente) {
        // L'ID non viene impostato qui in un'operazione di creazione/aggiornamento da DTO,
        // è gestito dal database o passato per l'aggiornamento.
        // dipendente.setId(dto.getId()); // Non usare per creazione, solo per fetch se necessario
        dipendente.setUsername(dto.getUsername());
        dipendente.setNome(dto.getNome());
        dipendente.setCognome(dto.getCognome());
        dipendente.setEmail(dto.getEmail());
        // L'URL dell'immagine viene gestito a parte, non mappato direttamente qui
        // dipendente.setImmagineProfiloUrl(dto.getImmagineProfiloUrl());
        return dipendente;
    }


    // --- Operazioni CRUD ---

    /**
     * Corrisponde all'operazione SAVE (Crea un nuovo dipendente).
     * @param dipendenteDto DTO contenente i dati del dipendente da salvare.
     * @return Il DTO del dipendente appena salvato.
     */

    public DipendenteDto save(DipendenteDto dipendenteDto) throws ValidationException {
        // Validazione unicità username e email
        if (dipendenteRepository.findByUsername(dipendenteDto.getUsername()).isPresent()) {
            throw new ValidationException("Username " + dipendenteDto.getUsername() + " già in uso");
        }
        if (dipendenteRepository.findByEmail(dipendenteDto.getEmail()).isPresent()) {
            throw new ValidationException("Email " + dipendenteDto.getEmail() + " già in uso");
        }

        Dipendente dipendente = new Dipendente();
        // Mappa i campi dal DTO all'entità
        dipendente = mapToDipendenteEntity(dipendenteDto, dipendente);
        // Imposta un'immagine predefinita se non fornita
        if (dipendenteDto.getImmagineProfiloUrl() == null || dipendenteDto.getImmagineProfiloUrl().isEmpty()) {
            dipendente.setImmagineProfiloUrl("https://ui-avatars.com/api/?name=" + dipendente.getNome() + "+" + dipendente.getCognome());
        } else {
            dipendente.setImmagineProfiloUrl(dipendenteDto.getImmagineProfiloUrl());
        }

       // sendMail(dipendenteDto.getEmail());
        Dipendente savedDipendente = dipendenteRepository.save(dipendente);

        return mapToDipendenteDto(savedDipendente);
    }


    public List<DipendenteDto> get() { // Questo è il metodo che il Controller cerca!
        return dipendenteRepository.findAll().stream()
                .map(this::mapToDipendenteDto)
                .collect(Collectors.toList());
    }
    /**
     * Corrisponde all'operazione GET (Recupera un dipendente tramite ID).
     * @param id ID del dipendente da recuperare.
     * @return Il DTO del dipendente trovato.
     * @throws NotFoundException se il dipendente non esiste.
     */

    public DipendenteDto get(Long id) throws NotFoundException { // Cambiato 'int' a 'Long' per l'ID
        Dipendente dipendente = dipendenteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Dipendente con ID " + id + " non trovato"));
        return mapToDipendenteDto(dipendente);
    }

    /**
     * Corrisponde all'operazione GET (Recupera tutti i dipendenti con paginazione).
     * @param pageable Oggetto Pageable per la paginazione.
     * @return Una pagina di DTO di dipendenti.
     */

    public Page<DipendenteDto> get(Pageable pageable) {
        return dipendenteRepository.findAll(pageable)
                .map(this::mapToDipendenteDto);
    }
    public Page<Dipendente> getAllDipendenti(int page, int size, String sortBy){
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return dipendenteRepository.findAll(pageable);
    }

    /**
     * Corrisponde all'operazione UPDATE (Aggiorna un dipendente esistente).
     * @param id ID del dipendente da aggiornare.
     * @param dipendenteDto DTO contenente i nuovi dati del dipendente.
     * @return Il DTO del dipendente aggiornato.
     * @throws NotFoundException se il dipendente non esiste.
     * @throws ValidationException se username o email sono già in uso da un altro dipendente.
     */

    public DipendenteDto update(Long id, DipendenteDto dipendenteDto) throws ValidationException, NotFoundException {
        Dipendente existingDipendente = dipendenteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Dipendente con ID " + id + " non trovato"));

        // Validazione unicità username e email (se modificati)
        if (!existingDipendente.getUsername().equals(dipendenteDto.getUsername()) && // Utilizza 'dipendenteDto'
                dipendenteRepository.findByUsername(dipendenteDto.getUsername()).isPresent()) {
            throw new ValidationException("Username " + dipendenteDto.getUsername() + " già in uso");
        }
        if (!existingDipendente.getEmail().equals(dipendenteDto.getEmail()) && // Utilizza 'dipendenteDto'
                dipendenteRepository.findByEmail(dipendenteDto.getEmail()).isPresent()) {
            throw new ValidationException("Email " + dipendenteDto.getEmail() + " già in uso");
        }

        // Aggiorna i campi dell'entità con i dati del DTO
        existingDipendente = mapToDipendenteEntity(dipendenteDto, existingDipendente);
        // L'URL dell'immagine non viene aggiornato qui, ma tramite il metodo patch apposito

        Dipendente updatedDipendente = dipendenteRepository.save(existingDipendente);
        return mapToDipendenteDto(updatedDipendente);
    }

    /**
     * Corrisponde all'operazione di aggiornamento dell'immagine profilo (PATCH).
     * Questo metodo carica l'immagine su Cloudinary e aggiorna l'URL nel dipendente.
     * @param dipendenteId ID del dipendente.
     * @param file File dell'immagine da caricare.
     * @return Il DTO del dipendente aggiornato con il nuovo URL dell'immagine.
     * @throws IOException se c'è un errore durante il caricamento del file.
     * @throws ValidationException se il file è vuoto o Cloudinary non è configurato.
     * @throws NotFoundException se il dipendente non è trovato.
     */

    public DipendenteDto updateImmagineProfilo(Long dipendenteId, MultipartFile file) throws IOException, ValidationException, NotFoundException { // Rinominato, cambiato 'int' a 'Long', restituisce DipendenteDto
        Dipendente dipendente = dipendenteRepository.findById(dipendenteId)
                .orElseThrow(() -> new NotFoundException("Dipendente con ID " + dipendenteId + " non trovato"));

        if (file.isEmpty()) {
            throw new ValidationException("Il file non può essere vuoto");
        }
        if (cloudinary == null) {
            throw new IllegalStateException("Cloudinary non è configurato. Impossibile caricare l'immagine.");
        }

        // Carica l'immagine su Cloudinary
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), Collections.emptyMap());
        String imageUrl = (String) uploadResult.get("secure_url"); // Usa "secure_url" per URL HTTPS

        dipendente.setImmagineProfiloUrl(imageUrl);
        Dipendente updatedDipendente = dipendenteRepository.save(dipendente);

        return mapToDipendenteDto(updatedDipendente); // Restituisce il DTO aggiornato
    }

    /**
     * Corrisponde all'operazione DELETE (Elimina un dipendente).
     * @param id ID del dipendente da eliminare.
     * @throws NotFoundException se il dipendente non esiste.
     */

    public void delete(Long id) throws NotFoundException {
        if (!dipendenteRepository.existsById(id)) {
            throw new NotFoundException("Dipendente con ID " + id + " non trovato");
        }
        dipendenteRepository.deleteById(id);
    }
    private void sendMail(String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Registrazione Servizio rest");
        message.setText("Registrazione al servizio rest avvenuta con successo");

        javaMailSender.send(message);
    }


}