package com.example.DocumentService.Document;


import com.example.DocumentService.Document.Exceptionhandler.GlobalExceptionHandler.*;
import com.example.DocumentService.DocumentDTO.CreateDocumentDTO;
import com.example.DocumentService.DocumentDTO.DocumentResponseDTO;
import com.example.DocumentService.DocumentDTO.UpdateDocumentDTO;
import com.example.DocumentService.DocumentDTO.UserDocumentsDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RequestMapping("/api/v1/document")
public class DocController {

    private final DocService docService;

    @GetMapping("/home")
    public String home() {
        return "Welcome to the Document Service!";
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<UserDocumentsDTO>> userAllDocuments(@RequestParam UUID ownerId) {
        List<UserDocumentsDTO> userDocuments = docService.getUserDocument(ownerId);
        ApiResponse<List<UserDocumentsDTO>> response = ApiResponse.success("User documents retrieved successfully", userDocuments);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<DocumentResponseDTO>> createDocument(@Valid @RequestBody CreateDocumentDTO createDocumentDTO) {
        DocumentResponseDTO newDoc = docService.createDocument(createDocumentDTO);
        ApiResponse<DocumentResponseDTO> response = ApiResponse.success("Document created successfully", newDoc);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<ApiResponse<DocumentResponseDTO>> userDocument(@PathVariable UUID id) {
        // Logic to retrieve a document by ID
        // This is just a placeholder, actual implementation will vary
        DocumentResponseDTO documentResponse = new DocumentResponseDTO(); // Replace with actual retrieval logic
        ApiResponse<DocumentResponseDTO> response = ApiResponse.success(documentResponse);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/udpate/{id}")
    public ResponseEntity<ApiResponse<DocumentResponseDTO>> updateResponse(@Valid @RequestBody UpdateDocumentDTO updateDocumentDTO) {
        try {
            DocumentResponseDTO documentResponseDTO = docService.updateDocument(updateDocumentDTO);
            ApiResponse<DocumentResponseDTO> response = ApiResponse.success("Document updated successfully", documentResponseDTO);
            return ResponseEntity.ok(response);
        } catch (DocumentNotFoundException ex) {
            ApiResponse<DocumentResponseDTO> response = ApiResponse.error("Invalid request: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (DocumentAccessDeniedException ex) {
            ApiResponse<DocumentResponseDTO> response = ApiResponse.error("Access denied: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } catch (Exception ex) {
            ApiResponse<DocumentResponseDTO> response = ApiResponse.error("Failed to update document: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> deleteDocument(@PathVariable UUID id) {
        // Logic to delete a document
        // This is just a placeholder, actual implementation will vary
        ApiResponse response = ApiResponse.success("Document deleted successfully");
        return ResponseEntity.ok(response);
    }

}
