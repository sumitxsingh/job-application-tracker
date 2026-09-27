package com.jobtracker.dto;

public final class CompanyDtos {

    private CompanyDtos() {}

    public record Response(
            Long id,
            String name,
            String website,
            String industry
    ) {}
}