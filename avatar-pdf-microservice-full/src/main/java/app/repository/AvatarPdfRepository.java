package app.repository;

import app.domain.AvatarPdf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AvatarPdfRepository extends JpaRepository<AvatarPdf, UUID> {
    Optional<AvatarPdf> findFirstByUserIdOrderByGeneratedAtDesc(UUID userId);
    List<AvatarPdf> findAllByUserIdOrderByGeneratedAtDesc(UUID userId);

    List<AvatarPdf> findAllByCreatedAtBefore(LocalDateTime threshold);
}
