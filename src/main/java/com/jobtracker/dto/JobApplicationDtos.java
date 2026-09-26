package com.jobtracker.dto;

import com.jobtracker.enums.ApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class JobApplicationDtos {

    private JobApplicationDtos() {}

    public record CreateRequest(
            @NotBlank String companyName,
            @NotBlank String jobTitle,
            LocalDate appliedDate,
            String source,
            BigDecimal salaryMin,
            BigDecimal salaryMax,
            String notes,
            String jobPostingUrl
    ) {}

    public record StatusUpdateRequest(
            @NotNull ApplicationStatus status,
            String note
    ) {}

    public record StageHistoryEntry(
            ApplicationStatus status,
            Instant changedAt,
            String note
    ) {}

    public record Response(
            Long id,
            String companyName,
            String jobTitle,
            ApplicationStatus status,
            LocalDate appliedDate,
            String source,
            BigDecimal salaryMin,
            BigDecimal salaryMax,
            String notes,
            String jobPostingUrl,
            Instant createdAt,
            Instant updatedAt,
            List<StageHistoryEntry> stageHistory
    ) {}

    public record Summary(
            Long id,
            String companyName,
            String jobTitle,
            ApplicationStatus status,
            LocalDate appliedDate,
            Instant updatedAt
    ) {}
}