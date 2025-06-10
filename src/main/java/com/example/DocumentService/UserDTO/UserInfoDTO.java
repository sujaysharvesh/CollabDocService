package com.example.DocumentService.UserDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.UUID;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserInfoDTO {
    @JsonProperty("id")
    private UUID userId;

    private String username;
    private String email;
}
