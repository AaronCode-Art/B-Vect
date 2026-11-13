package com.vect.vect.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyMfaRequest(
    @NotBlank String challengeToken,
    @NotBlank @Pattern(regexp = "\\d{6}") String code
) {}
