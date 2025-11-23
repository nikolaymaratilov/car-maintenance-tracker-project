package app.service;

import app.dto.AvatarPdfCreateRequest;
import app.dto.AvatarPdfResponse;
import app.dto.AvatarPdfUpdateRequest;
import app.entity.AvatarPdf;
import app.exception.ResourceNotFoundException;
import app.repository.AvatarPdfRepository;
import com.example.avatarpdf.util.PdfGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AvatarPdfService {

    private static final Logger log = LoggerFactory.getLogger(AvatarPdfService.class);
    private final AvatarPdfRepository repository;

    public AvatarPdfService(AvatarPdfRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public AvatarPdfResponse create(AvatarPdfCreateRequest request) {
        log.info("Generating avatar PDF for userId={}", request.getUserId());

        byte[] pdf = PdfGenerator.generateFromImageUrl(request.getImageUrl(), request.getDisplayName());

        AvatarPdf entity = new AvatarPdf();
        entity.setUserId(request.getUserId());
        entity.setImageUrl(request.getImageUrl());
        entity.setPdfContent(pdf);

        repository.save(entity);

        return new AvatarPdfResponse(entity.getId(), entity.getUserId(),
                "/api/avatar-pdfs/" + entity.getId() + "?userId=" + entity.getUserId());
    }

    @Transactional
    public AvatarPdfResponse update(UUID id, UUID userId, AvatarPdfUpdateRequest request) {
        log.info("Regenerating avatar PDF id={} for userId={}", id, userId);

        AvatarPdf entity = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("AvatarPdf not found for user."));

        byte[] pdf = PdfGenerator.generateFromImageUrl(request.getImageUrl(), request.getDisplayName());

        entity.setImageUrl(request.getImageUrl());
        entity.setPdfContent(pdf);

        repository.save(entity);

        return new AvatarPdfResponse(entity.getId(), entity.getUserId(),
                "/api/avatar-pdfs/" + entity.getId() + "?userId=" + entity.getUserId());
    }

    @Transactional(readOnly = true)
    public AvatarPdf getById(UUID id, UUID userId) {
        return repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("AvatarPdf not found."));
    }

    @Transactional
    public void delete(UUID id, UUID userId) {
        log.info("Deleting avatar PDF id={} for userId={}", id, userId);

        AvatarPdf entity = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("AvatarPdf not found."));
        repository.delete(entity);
    }
}
