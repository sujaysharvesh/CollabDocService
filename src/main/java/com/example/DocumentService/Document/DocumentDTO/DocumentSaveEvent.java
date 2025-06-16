package com.example.DocumentService.Document.DocumentDTO;


import lombok.*;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Getter
@Setter
public class DocumentSaveEvent {
    private String documentId;
    private String userId;
    private byte[] content;
    private Instant timestamp;
}
