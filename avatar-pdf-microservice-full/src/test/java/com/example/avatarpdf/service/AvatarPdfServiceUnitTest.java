package com.example.avatarpdf.service;

import com.example.avatarpdf.dto.AvatarPdfCreateRequest;
import com.example.avatarpdf.repository.AvatarPdfRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AvatarPdfServiceUnitTest {

    @Test
    void create_throws_when_imageUrl_blank() {
        AvatarPdfRepository repo = Mockito.mock(AvatarPdfRepository.class);
        AvatarPdfService service = new AvatarPdfService(repo);

        AvatarPdfCreateRequest req = new AvatarPdfCreateRequest();
        req.setUserId(java.util.UUID.randomUUID());
        req.setImageUrl("");

        assertThrows(RuntimeException.class, () -> service.create(req));
    }
}
