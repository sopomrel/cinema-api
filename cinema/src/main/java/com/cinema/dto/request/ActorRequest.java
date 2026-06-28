package com.cinema.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActorRequest {

    @NotNull(message = "{validation.firstName.required}")
    @Size(min = 2, max = 50, message = "{validation.firstName.size}")
    private String firstName;

    @NotNull(message = "{validation.lastName.required}")
    @Size(min = 2, max = 50, message = "{validation.firstName.size}")
    private String lastName;

    @NotNull(message = "{validation.email.required}")
    @Email(message = "{validation.email.invalid}")
    private String email;

    @Size(max = 50, message = "{validation.firstName.size}")
    private String nationality;
}
