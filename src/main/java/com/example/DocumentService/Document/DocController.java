package com.example.DocumentService.Document;


import com.example.DocumentService.Document.Exceptionhandler.GlobalExceptionHandler.*;
import com.example.DocumentService.DocumentDTO.CreateDocumentDTO;
import com.example.DocumentService.DocumentDTO.DocumentResponseDTO;
import com.example.DocumentService.DocumentDTO.UpdateDocumentDTO;
import com.example.DocumentService.DocumentDTO.UserDocumentsDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RequestMapping("/api/v1/document")
@RestController
@Slf4j
public class DocController {

    private final DocService docService;

    @GetMapping("/home")
    public String home() {
        return "Welcome to the Document Service!";
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<UserDocumentsDTO>>> userAllDocuments(@PathVariable UUID ownerId) {
        try {
            List<UserDocumentsDTO> userDocuments = docService.getUserAllDocuments(ownerId);
            ApiResponse<List<UserDocumentsDTO>> response = ApiResponse.success("User documents retrieved successfully", userDocuments);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            ApiResponse<List<UserDocumentsDTO>> response = ApiResponse.error("Failed to retrieve user documents: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<DocumentResponseDTO>> createDocument(@Valid @RequestBody CreateDocumentDTO createDocumentDTO) {
        try {
            DocumentResponseDTO newDoc = docService.createDocument(createDocumentDTO);
            ApiResponse<DocumentResponseDTO> response = ApiResponse.success("Document created successfully", newDoc);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            ApiResponse<DocumentResponseDTO> response = ApiResponse.error("Failed to create document: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    }

    @GetMapping("/get/{docId}")
    public ResponseEntity<ApiResponse<DocumentResponseDTO>> userDocument(@PathVariable UUID docId, @RequestBody UUID ownerId) {
        try {
            DocumentResponseDTO documentResponseDTO = docService.getUserDocument(docId, ownerId);
            ApiResponse<DocumentResponseDTO> response = ApiResponse.success("Document retrieved successfully", documentResponseDTO);
            return ResponseEntity.ok(response);
        } catch (DocumentNotFoundException ex) {
            ApiResponse<DocumentResponseDTO> response = ApiResponse.error("Document not found: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception ex) {
            ApiResponse<DocumentResponseDTO> response = ApiResponse.error("Failed to retrieve document: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/update/{docId}")
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

    @DeleteMapping("/delete/{docId}")
    public ResponseEntity<ApiResponse<String>> deleteDocument(@PathVariable UUID docId, @RequestBody UUID ownerId) {
        docService.deleteDocument(docId, ownerId);
        ApiResponse<String> response = ApiResponse.success("Document deleted successfully", "Document with ID " + docId + " has been deleted.");
        return ResponseEntity.ok(response);
    }

}
