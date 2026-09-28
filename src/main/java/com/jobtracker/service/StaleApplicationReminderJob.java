package com.jobtracker.service;

import com.jobtracker.entity.JobApplication;
import com.jobtracker.repository.JobApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Runs daily at 09:00 and flags in-flight applications with no update for 14+ days.
 * For now it only logs; the same query could feed an email/notification later.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StaleApplicationReminderJob {

    private static final int STALE_AFTER_DAYS = 14;

    private final JobApplicationRepository applicationRepository;

    @Scheduled(cron = "${jobtracker.stale-reminder.cron:0 0 9 * * *}")
    public void flagStaleApplications() {
        Instant threshold = Instant.now().minus(STALE_AFTER_DAYS, ChronoUnit.DAYS);
        List<JobApplication> stale = applicationRepository.findAllStale(threshold);

        if (stale.isEmpty()) {
            log.info("Stale application check: nothing to follow up on");
            return;
        }

        Map<Long, Long> countByUser = stale.stream()
                .collect(Collectors.groupingBy(a -> a.getUser().getId(), Collectors.counting()));

        countByUser.forEach((userId, count) ->
                log.info("Follow-up reminder: user {} has {} application(s) with no update for {}+ days",
                        userId, count, STALE_AFTER_DAYS));
    }
}