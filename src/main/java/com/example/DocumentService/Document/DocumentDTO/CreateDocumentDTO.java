package com.example.DocumentService.Document.DocumentDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class CreateDocumentDTO {

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Content is required")
    private byte[] content;

}