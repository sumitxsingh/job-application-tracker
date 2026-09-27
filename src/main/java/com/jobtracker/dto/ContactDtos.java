package com.jobtracker.dto;

import jakarta.validation.constraints.NotBlank;

public final class ContactDtos {

    private ContactDtos() {}

    public record CreateRequest(
            @NotBlank String name,
            String email,
            String phone,
            String roleTitle
    ) {}

    public record Response(
            Long id,
            Long companyId,
            String name,
            String email,
            String phone,
            String roleTitle
    ) {}
}