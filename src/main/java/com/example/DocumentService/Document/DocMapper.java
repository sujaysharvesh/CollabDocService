package com.example.DocumentService.Document;


import com.example.DocumentService.Document.DocumentDTO.DocumentResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class DocMapper {

    public DocumentResponseDTO toDocumentResponseDTO(Document document) {
        if (document == null) {
            return null;
        }

        DocumentResponseDTO response = DocumentResponseDTO.builder()
                .id(document.getId())
                .title(document.getTitle())
                .content(document.getContent())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .ownerId(document.getOwnerId())
                .build();

        return response;
    }

}
