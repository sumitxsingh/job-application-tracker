package com.jobtracker.dto;

import com.jobtracker.enums.InterviewType;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public final class InterviewDtos {

    private InterviewDtos() {}

    public record ScheduleRequest(
            @NotNull InterviewType type,
            Integer round,
            @NotNull Instant scheduledAt,
            String interviewerName
    ) {}

    public record FeedbackRequest(
            String feedback
    ) {}

    public record Response(
            Long id,
            Long applicationId,
            InterviewType type,
            Integer round,
            Instant scheduledAt,
            String interviewerName,
            String feedback
    ) {}
}