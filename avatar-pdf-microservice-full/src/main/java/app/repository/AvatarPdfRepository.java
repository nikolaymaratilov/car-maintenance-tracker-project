package app.repository;

import app.entity.AvatarPdf;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AvatarPdfRepository extends JpaRepository<AvatarPdf, UUID> {
    Optional<AvatarPdf> findByIdAndUserId(UUID id, UUID userId);
}
