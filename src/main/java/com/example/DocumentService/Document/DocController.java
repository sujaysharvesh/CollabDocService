package com.example.DocumentService.Document;


import com.example.DocumentService.Document.Client.UserServiceClient;
import com.example.DocumentService.Document.DocumentDTO.*;
import com.example.DocumentService.Document.Exceptionhandler.GlobalExceptionHandler.*;
import com.example.DocumentService.UserDTO.UserInfoDTO;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RequestMapping("/api/v1/document")
@RestController
@Slf4j
public class DocController {

    private final DocService docService;
    private final UserServiceClient userServiceClient;

    @GetMapping("/home")
    public String home() {
        return "Welcome to the Document Service!";
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<DocumentResponseDTO>>> userDocument(@RequestBody RequestUserDocument requestUserDocument,
                                                                               @RequestHeader("Authorization") String token) {
        return userServiceClient.getUserInfo(token)
                .map(userInfoDTO -> {
                    DocumentResponseDTO documentResponseDTO = docService.getUserDocument(requestUserDocument.getDocumentId(), userInfoDTO.getUserId());
                    ApiResponse<DocumentResponseDTO> response = ApiResponse.success("Document retrieved successfully", documentResponseDTO);
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(DocumentNotFoundException.class, ex -> {
                    ApiResponse<DocumentResponseDTO> response = ApiResponse.error("Document not found: " + ex.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(response));
                })
                .onErrorResume(DocumentAccessDeniedException.class, ex -> {
                    ApiResponse<DocumentResponseDTO> response = ApiResponse.error("Access denied: " + ex.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(response));
                })
                .onErrorResume(Exception.class, ex -> {
                    ApiResponse<DocumentResponseDTO> response = ApiResponse.error("Failed to retrieve document: " + ex.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
                });
    }
    
    

    @GetMapping("/currentUser")
    public Mono<ResponseEntity<ApiResponse<UserInfoDTO>>> currentUser(@RequestHeader("Authorization") String token) {
        if (token == null || token.isEmpty()) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication token is missing or invalid")));
        }
        return userServiceClient.getUserInfo(token)
                .map(userInfoDTO -> {
                    ApiResponse<UserInfoDTO> response = ApiResponse.success("Current user retrieved successfully", userInfoDTO);
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(ServiceUnavailableException.class, ex -> {
                    log.error("Service unavailable: {}", ex.getMessage());
                    ApiResponse<UserInfoDTO> response = ApiResponse.error("Service unavailable: " + ex.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
                })
                .onErrorResume(WebClientResponseException.Unauthorized.class, ex -> {
                    log.error("Unauthorized access: {}", ex.getMessage());
                    ApiResponse<UserInfoDTO> response = ApiResponse.error("Unauthorized access: " + ex.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response));
                })
                .onErrorResume(WebClientResponseException.NotFound.class, ex -> {
                    log.error("User not found: {}", ex.getMessage());
                    ApiResponse<UserInfoDTO> response = ApiResponse.error("User not found: " + ex.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(response));
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("Unexpected error: {}", ex.getMessage());
                    ApiResponse<UserInfoDTO> response = ApiResponse.error("Unexpected error: " + ex.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
                });
    }

    @GetMapping("/all")
    public Mono<ResponseEntity<ApiResponse<List<UserDocumentsDTO>>>> userAllDocuments(@RequestHeader("Authorization") String token) {
        validateToken(token);
        return userServiceClient.getUserInfo(token).flatMap(
                userInfoDTO -> {
                List<UserDocumentsDTO> userDocuments = docService.getUserAllDocuments(userInfoDTO.getUserId());
                    ApiResponse<List<UserDocumentsDTO>> response = ApiResponse.success("User documents retrieved successfully", userDocuments);
                return Mono.just(ResponseEntity.ok(response));
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("Error retrieving user documents: {}", ex.getMessage());
                    ApiResponse<List<UserDocumentsDTO>> response = ApiResponse.error("Failed to retrieve user documents: " + ex.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
                });
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<DocumentResponseDTO>>> createDocument(@RequestHeader("Authorization") String token,
                                                                                 @Valid @RequestBody CreateDocumentDTO createDocumentDTO) {


        try {
            String validatedToken = validateToken(token);
            if (validatedToken == null || validatedToken.isEmpty()) {
                return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Authentication token is missing or invalid")));
            }
            return userServiceClient.getUserInfo(token).map(userInfoDTO -> {
                        DocumentResponseDTO documentResponseDTO = docService.createDocument(createDocumentDTO, userInfoDTO.getUserId());
                        ApiResponse<DocumentResponseDTO> response = ApiResponse
                                .success("Document created successfully", documentResponseDTO);
                        return ResponseEntity.status(HttpStatus.CREATED).body(response);
                    })
                    .onErrorResume(ServiceUnavailableException.class, ex -> {
                        log.error("Service unavailable: {}", ex.getMessage());
                        ApiResponse<DocumentResponseDTO> response = ApiResponse
                                .error("Service unavailable: " + ex.getMessage());
                        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
                    })
                    .onErrorResume(WebClientResponseException.Unauthorized.class, ex -> {
                        log.error("Unauthorized access: {}", ex.getMessage());
                        ApiResponse<DocumentResponseDTO> response = ApiResponse
                                .error("Unauthorized access: " + ex.getMessage());
                        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response));
                    })
                    .onErrorResume(Exception.class, ex -> {
                        log.error("Failed to create document: {}", ex.getMessage());
                        ApiResponse<DocumentResponseDTO> response = ApiResponse
                                .error("Failed to create document: " + ex.getMessage());
                        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
                    });
        } catch (ValidationException exception) {
            log.error("Validation error: {}", exception.getMessage());
            ApiResponse<DocumentResponseDTO> response = ApiResponse.error("Validation error: " + exception.getMessage());
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
        } catch (Exception ex) {
            log.error("Unexpected error: {}", ex.getMessage());
            ApiResponse<DocumentResponseDTO> response = ApiResponse.error("Unexpected error: " + ex.getMessage());
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
        }

    }
    
    @PatchMapping
    public Mono<ResponseEntity<ApiResponse<DocumentResponseDTO>>> updateResponse(@Valid @RequestBody
                                                                                     UpdateDocumentDTO updateDocumentDTO,
                                                                                 @RequestHeader("Authorization") String token) {
        return userServiceClient.getUserInfo(token).map(userInfoDTO -> {
            updateDocumentDTO.setOwnerId(userInfoDTO.getUserId());
            DocumentResponseDTO updatedDocument = docService.updateDocument(updateDocumentDTO);
            ApiResponse<DocumentResponseDTO> response = ApiResponse.success("Document updated successfully", updatedDocument);
            return ResponseEntity.ok(response);
        })
                .onErrorResume(DocumentNotFoundException.class, ex -> {
                    ApiResponse<DocumentResponseDTO> response = ApiResponse.error("Document not found: " + ex.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(response));
                })
                .onErrorResume(DocumentAccessDeniedException.class, ex -> {
                    ApiResponse<DocumentResponseDTO> response = ApiResponse.error("Access denied: " + ex.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(response));
                })
                .onErrorResume(Exception.class, ex -> {
                    ApiResponse<DocumentResponseDTO> response = ApiResponse.error("Failed to update document: " + ex.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
                });
    }

    @DeleteMapping
    public Mono<ResponseEntity<ApiResponse<String>>> deleteDocument(@RequestHeader("Authorization") String token, @PathVariable UUID docId) {
        return userServiceClient.getUserInfo(token).map(userInfoDTO -> {
           docService.deleteDocument(docId, userInfoDTO.getUserId());
           ApiResponse<String> response = ApiResponse.success("Document Deleted Succussfully");
           return ResponseEntity.ok(response);
        });
    }

    private String validateToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new ValidationException("Authentication token is missing or invalid");
        }
        return token;
    }
}
