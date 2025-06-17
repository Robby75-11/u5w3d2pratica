package it.epicode.u5w3d2pratica.service;

import it.epicode.u5w3d2pratica.dto.ViaggioDto;
import it.epicode.u5w3d2pratica.exception.NotFoundException;
import it.epicode.u5w3d2pratica.exception.ValidationException;
import it.epicode.u5w3d2pratica.model.Viaggio;
import it.epicode.u5w3d2pratica.repository.ViaggioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ViaggioService {

    private static final Logger logger = LoggerFactory.getLogger(ViaggioService.class);

    @Autowired
    private ViaggioRepository viaggioRepository;

    // --- Metodi Helper per la Mappatura ---

    /**
     * Mappa un'entità {@link Viaggio} a un {@link ViaggioDto}.
     * @param viaggio L'entità Viaggio.
     * @return Il corrispondente ViaggioDto.
     */
    private ViaggioDto mapToViaggioDto(Viaggio viaggio) {
        ViaggioDto dto = new ViaggioDto();
        dto.setId(viaggio.getId());
        dto.setDestinazione(viaggio.getDestinazione());
        dto.setData(viaggio.getData());
        dto.setStatoViaggio(viaggio.getStatoViaggio());
        return dto;
    }

    /**
     * Mappa un {@link ViaggioDto} a un'entità {@link Viaggio}.
     * Questo metodo è usato per creare o aggiornare un'entità da un DTO.
     * NON imposta l'ID, poiché viene gestito dal database o da operazioni di aggiornamento esplicite.
     * @param dto Il ViaggioDto.
     * @param viaggio L'entità Viaggio da popolare (può essere una nuova istanza o una esistente).
     * @return L'entità Viaggio popolata.
     */
    private Viaggio mapToViaggioEntity(ViaggioDto dto, Viaggio viaggio) {
        viaggio.setDestinazione(dto.getDestinazione());
        viaggio.setData(dto.getData());
        viaggio.setStatoViaggio(dto.getStatoViaggio());
        return viaggio;
    }

    // --- Operazioni CRUD ---

    /**
     * Crea un nuovo piano di viaggio nel sistema.
     * @param viaggioDto DTO contenente i dati del piano di viaggio da salvare.
     * @return Il DTO del piano di viaggio appena creato.
     * @throws ValidationException se la data del viaggio è nel passato.
     */

    public ViaggioDto save(ViaggioDto viaggioDto) throws ValidationException {
        // Validazione: La data del viaggio non può essere nel passato per un nuovo piano.
        if (viaggioDto.getData().isBefore(LocalDate.now())) {
            logger.error("Tentativo di creare un viaggio con data nel passato: {}", viaggioDto);
            throw new ValidationException("La data del viaggio non può essere nel passato.");
        }
        // Il tuo DTO ha @NotNull per statoViaggio, quindi dovrebbe essere sempre presente.
        // Se non lo fosse, potresti impostare un valore predefinito qui:
        // if (viaggioDto.getStatoViaggio() == null) {
        //      viaggioDto.setStatoViaggio(StatoViaggio.PIANIFICATO);
        // }

        Viaggio viaggio = new Viaggio();
        viaggio = mapToViaggioEntity(viaggioDto, viaggio); // Mappa DTO all'entità

        Viaggio savedViaggio = viaggioRepository.save(viaggio);
        logger.info("Viaggio creato con ID: {}", savedViaggio.getId());
        return mapToViaggioDto(savedViaggio); // Restituisce il DTO dell'entità salvata
    }

    /**
     * Recupera un elenco di tutti i piani di viaggio.
     * @return Una {@link List} di {@link ViaggioDto} che rappresenta tutti i piani di viaggio.
     */

    public List<ViaggioDto> get() {
        List<ViaggioDto> viaggi = viaggioRepository.findAll().stream()
                .map(this::mapToViaggioDto)
                .collect(Collectors.toList());
        logger.info("Recuperati {} viaggi.", viaggi.size());
        return viaggi;
    }

    /**
     * Recupera un singolo piano di viaggio tramite il suo ID univoco.
     * @param id L'ID del piano di viaggio da recuperare.
     * @return Il {@link ViaggioDto} del piano di viaggio trovato.
     * @throws NotFoundException se non viene trovato alcun piano di viaggio con l'ID fornito.
     */

    public ViaggioDto get(Long id) throws NotFoundException {
        Viaggio viaggio = viaggioRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Viaggio con ID {} non trovato.", id);
                    return new NotFoundException("Piano di viaggio con ID " + id + " non trovato");
                });
        logger.info("Viaggio con ID {} recuperato.", id);
        return mapToViaggioDto(viaggio);
    }

    /**
     * Recupera un elenco paginato di tutti i piani di viaggio.
     * @param pageable Oggetto {@link Pageable} per la paginazione e l'ordinamento.
     * @return Una {@link Page} di {@link ViaggioDto} che rappresenta i piani di viaggio paginati.
     */

    public Page<ViaggioDto> get(Pageable pageable) {
        Page<ViaggioDto> viaggiPage = viaggioRepository.findAll(pageable)
                .map(this::mapToViaggioDto);
        logger.info("Recuperata pagina {} di viaggi (dimensione: {}).", pageable.getPageNumber(), pageable.getPageSize());
        return viaggiPage;
    }

    /**
     * Aggiorna un piano di viaggio esistente.
     * @param id L'ID del piano di viaggio da aggiornare.
     * @param viaggioDto DTO contenente i nuovi dati per il piano di viaggio.
     * @return Il {@link ViaggioDto} del piano di viaggio aggiornato.
     * @throws NotFoundException se il piano di viaggio con l'ID fornito non esiste.
     * @throws ValidationException se la data di viaggio aggiornata non è valida (ad esempio, si tenta di impostare un viaggio futuro nel passato).
     */

    public ViaggioDto update(Long id, ViaggioDto viaggioDto) throws NotFoundException, ValidationException {
        Viaggio existingViaggio = viaggioRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Viaggio con ID {} non trovato per l'aggiornamento.", id);
                    return new NotFoundException("Piano di viaggio con ID " + id + " non trovato");
                });

        // Validazione: Non è possibile spostare una data di viaggio futura nel passato.
        // Se la data del viaggio è nel futuro E la nuova data è nel passato, lancia un'eccezione.
        if (existingViaggio.getData().isAfter(LocalDate.now()) && viaggioDto.getData().isBefore(LocalDate.now())) {
            logger.error("Tentativo di spostare un viaggio futuro (ID: {}) nel passato. Nuova data: {}", id, viaggioDto.getData());
            throw new ValidationException("Impossibile impostare una data di viaggio futura nel passato.");
        }
        // Potresti aggiungere qui altre logiche di validazione, ad esempio se non puoi cambiare la data
        // di un viaggio già AVVIATO o COMPLETATO.

        existingViaggio = mapToViaggioEntity(viaggioDto, existingViaggio); // Mappa DTO all'entità esistente

        Viaggio updatedViaggio = viaggioRepository.save(existingViaggio);
        logger.info("Viaggio con ID {} aggiornato.", updatedViaggio.getId());
        return mapToViaggioDto(updatedViaggio);
    }

    /**
     * Elimina un piano di viaggio.
     * @param id L'ID del piano di viaggio da eliminare.
     * @throws NotFoundException se il piano di viaggio con l'ID fornito non esiste.
     */

    public void delete(Long id) throws NotFoundException {
        if (!viaggioRepository.existsById(id)) {
            logger.warn("Tentativo di eliminare un viaggio inesistente con ID: {}", id);
            throw new NotFoundException("Piano di viaggio con ID " + id + " non trovato");
        }
        viaggioRepository.deleteById(id);
        logger.info("Viaggio con ID {} eliminato.", id);
    }
}