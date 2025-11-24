package app.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PdfGenerationException.class)
    public ResponseEntity<String> handlePdfGeneration(PdfGenerationException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
