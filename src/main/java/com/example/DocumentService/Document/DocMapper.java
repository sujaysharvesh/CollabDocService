package com.example.DocumentService.Document;


import com.example.DocumentService.Document.DocumentDTO.DocumentResponseDTO;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DocMapper {

    public DocumentResponseDTO toDocumentResponseDTO(Document document) {
        if (document == null) {
            return null;
        }

        DocumentResponseDTO response = DocumentResponseDTO.builder()
                .documentId(document.getId().toString())
                .title(document.getTitle())
                .content(document.getContent())
                .updatedAt(document.getUpdatedAt())
                .build();

        return response;
    }

}
