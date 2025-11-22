# Avatar PDF Microservice (REST)

Standalone Spring Boot microservice that generates and stores a PDF containing a user's avatar image.

## Tech stack
- Java 17
- Spring Boot 3.4.0
- Maven
- MySQL (separate DB)
- Spring Data JPA
- Apache PDFBox

## Domain entity
- `AvatarPdf` (UUID id)

## Functionalities (microservice)
1. **Generate avatar PDF**  
   `POST /api/avatar-pdfs`  
   Creates a new pdf for a user from `imageUrl` and stores it.

2. **Regenerate / update avatar PDF**  
   `PUT /api/avatar-pdfs/{id}?userId=...`  
   Replaces pdf content with a new avatar.

3. **Delete avatar PDF**  
   `DELETE /api/avatar-pdfs/{id}?userId=...`

4. **Download avatar PDF (read-only)**  
   `GET /api/avatar-pdfs/{id}?userId=...`  
   Returns `application/pdf`.

## Validation & error handling
- DTO validation with Bean Validation
- Built-in handler: `MethodArgumentNotValidException`
- Custom handlers: `PdfGenerationException`, `ResourceNotFoundException`

## Run
Update DB credentials in `application.yml`, then:

```bash
mvn spring-boot:run
```

Runs on port **8082**.
