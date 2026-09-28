package com.jobtracker.service;

import com.jobtracker.dto.DashboardStatsResponse;
import com.jobtracker.entity.JobApplication;
import com.jobtracker.enums.ApplicationStatus;
import com.jobtracker.repository.JobApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final Set<ApplicationStatus> REACHED_INTERVIEW = Set.of(
            ApplicationStatus.INTERVIEWING, ApplicationStatus.OFFERED, ApplicationStatus.REJECTED
    );

    private final JobApplicationRepository applicationRepository;

    public DashboardStatsResponse getStats(Long userId) {
        List<JobApplication> applications = applicationRepository.findByUserId(userId);
        long total = applications.size();

        Map<ApplicationStatus, Long> byStatus = applications.stream()
                .collect(Collectors.groupingBy(
                        JobApplication::getStatus,
                        () -> new EnumMap<>(ApplicationStatus.class),
                        Collectors.counting()
                ));

        Map<String, Long> countByStatus = byStatus.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().name(),
                        (Map.Entry<ApplicationStatus, Long> e) -> e.getValue()
                ));

        long reachedInterview = applications.stream()
                .filter(a -> REACHED_INTERVIEW.contains(a.getStatus()) || a.getStatus() == ApplicationStatus.OFFERED)
                .count();

        long offered = byStatus.getOrDefault(ApplicationStatus.OFFERED, 0L);

        double interviewRate = total == 0 ? 0.0 : round((double) reachedInterview / total * 100);
        double offerRate = total == 0 ? 0.0 : round((double) offered / total * 100);

        Instant staleThreshold = Instant.now().minus(14, ChronoUnit.DAYS);
        long stale = applicationRepository.findStaleApplications(userId, staleThreshold).size();

        return new DashboardStatsResponse(total, countByStatus, interviewRate, offerRate, stale);
    }

    private double round(double value) {
        return Math.round(value * 10) / 10.0;
    }
}