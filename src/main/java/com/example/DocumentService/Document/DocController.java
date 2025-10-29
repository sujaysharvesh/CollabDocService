package com.example.DocumentService.Document;


import com.example.DocumentService.Document.Client.UserServiceClient;
import com.example.DocumentService.Document.DocumentDTO.*;
import com.example.DocumentService.Document.Exceptionhandler.GlobalExceptionHandler.*;
import com.example.DocumentService.UserDTO.UserInfoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.WebsocketClientSpec;

import java.nio.charset.StandardCharsets;
import java.util.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/document")
@RestController
@Slf4j
public class DocController {

    private final DocService docService;
    private final UserServiceClient userServiceClient;
    private final DocRepository docRepository;

    @GetMapping("/home")
    public String home() {
        return "Welcome to the Document Service!";
    }


    @GetMapping("/test/{documentId}")
    public ResponseEntity<Map<String, Object>> getDocumentContent(@PathVariable UUID documentId) {
        Optional<Document> doc = docRepository.findById(documentId);
        Map<String, Object> response = new HashMap<>();
        if (doc.get().getContent() != null) {
            byte[] content = doc.get().getContent();
            String jsonContent = new String(content, StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                Map<String, Object> ResponseContent = objectMapper.readValue(jsonContent, Map.class);
                response.put("content", ResponseContent);
            } catch (Exception e) {
                response.put("error", Map.of("error", jsonContent));
            }
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{documentId}")
    public Mono<ResponseEntity<ApiResponse<DocumentResponseDTO>>> userDocument(@PathVariable UUID documentId,
                                                                               @RequestHeader("Authorization") String token) {
                return userServiceClient.getUserInfo(token)
                .map(userInfoDTO -> {
                    DocumentResponseDTO documentResponseDTO = docService.getUserDocument(documentId, userInfoDTO.getUserId());
                    ApiResponse<DocumentResponseDTO> response = ApiResponse.success("Document retrieved successfully", documentResponseDTO);
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(ex -> handleError(ex, "User Documents"));
    }

    @GetMapping("/{documentId}/{userId}")
    public ResponseEntity<ApiResponse<DocumentResponseDTO>> userDocumentContent(@PathVariable UUID documentId,
                                                                   @PathVariable UUID userId) {
        DocumentResponseDTO responseDTO = docService.getUserDocument(documentId, userId);
        ApiResponse response = ApiResponse.success(responseDTO);
        return ResponseEntity.ok(response);
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
                .onErrorResume(ex -> handleError(ex, "Get Current User"));
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
                    .onErrorResume(ex -> handleError(ex, "Create Document"));
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
                .onErrorResume(ex -> handleError(ex, "Update Document"));
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

    private <T> Mono<ResponseEntity<ApiResponse<T>>> handleError(Throwable ex, String context) {
        log.error("{}: {}", context, ex.getMessage());
        String message = switch (ex) {
            case ServiceUnavailableException e -> "Service Unavailable " + e.getMessage();
            case WebClientResponseException.NotFound e -> "User Not Found " + e.getMessage();
            case DocumentNotFoundException e -> "Document Not Found " + e.getMessage();
            case WebClientResponseException.Unauthorized e -> "Unauthorized Access";
            default -> "Unexpected Error" + ex.getMessage();
        };

        HttpStatus status = ex instanceof ServiceUnavailableException ? HttpStatus.SERVICE_UNAVAILABLE :
                            ex instanceof WebClientResponseException.Unauthorized ? HttpStatus.UNAUTHORIZED :
                            ex instanceof WebClientResponseException.NotFound ? HttpStatus.NOT_FOUND :
                            ex instanceof  DocumentNotFoundException ? HttpStatus.NOT_FOUND :
                                    HttpStatus.INTERNAL_SERVER_ERROR;

        return Mono.just(ResponseEntity.status(status).body(ApiResponse.error(message)));

    }
}
