package com.jobtracker.dto;

import java.util.Map;

public record DashboardStatsResponse(
        long totalApplications,
        Map<String, Long> countByStatus,
        double interviewRate,
        double offerRate,
        long staleApplications
) {}