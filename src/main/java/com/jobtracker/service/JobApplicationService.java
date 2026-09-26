package com.jobtracker.service;

import com.jobtracker.dto.JobApplicationDtos.*;
import com.jobtracker.entity.ApplicationStageHistory;
import com.jobtracker.entity.Company;
import com.jobtracker.entity.JobApplication;
import com.jobtracker.entity.User;
import com.jobtracker.enums.ApplicationStatus;
import com.jobtracker.exception.InvalidTransitionException;
import com.jobtracker.exception.ResourceNotFoundException;
import com.jobtracker.repository.CompanyRepository;
import com.jobtracker.repository.JobApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final CompanyRepository companyRepository;

    @Transactional
    public Response create(User user, CreateRequest request) {
        Company company = companyRepository.findByNameIgnoreCase(request.companyName())
                .orElseGet(() -> companyRepository.save(
                        Company.builder().name(request.companyName()).build()));

        JobApplication application = JobApplication.builder()
                .user(user)
                .company(company)
                .jobTitle(request.jobTitle())
                .status(ApplicationStatus.APPLIED)
                .appliedDate(request.appliedDate())
                .source(request.source())
                .salaryMin(request.salaryMin())
                .salaryMax(request.salaryMax())
                .notes(request.notes())
                .jobPostingUrl(request.jobPostingUrl())
                .build();

        application.getStageHistory().add(
                ApplicationStageHistory.builder()
                        .application(application)
                        .status(ApplicationStatus.APPLIED)
                        .note("Application created")
                        .build()
        );

        return toResponse(applicationRepository.save(application));
    }

    public List<Summary> listForUser(Long userId) {
        return applicationRepository.findByUserId(userId).stream()
                .map(this::toSummary)
                .toList();
    }

    public Response getOwned(Long userId, Long applicationId) {
        return toResponse(findOwned(userId, applicationId));
    }

    @Transactional
    public Response updateStatus(Long userId, Long applicationId, StatusUpdateRequest request) {
        JobApplication application = findOwned(userId, applicationId);

        StatusTransitionResult result = applyStatusTransition(application.getStatus(), request.status());

        switch (result) {
            case StatusTransitionResult.Rejected rejected ->
                throw new InvalidTransitionException(rejected.reason());
            case StatusTransitionResult.Accepted accepted -> {
                application.setStatus(accepted.to());
                application.getStageHistory().add(
                        ApplicationStageHistory.builder()
                                .application(application)
                                .status(accepted.to())
                                .note(request.note())
                                .build()
                );
            }
        }

        return toResponse(applicationRepository.save(application));
    }

    private StatusTransitionResult applyStatusTransition(ApplicationStatus from, ApplicationStatus to) {
        record Transition(ApplicationStatus from, ApplicationStatus to) {}

        return switch (new Transition(from, to)) {
            case Transition(var f, var t) when f == t ->
                new StatusTransitionResult.Rejected(f, t, "Application is already in status " + t);

            case Transition(var f, var t) when f == ApplicationStatus.REJECTED || f == ApplicationStatus.WITHDRAWN ->
                new StatusTransitionResult.Rejected(f, t, "Cannot change status of a " + f + " application - it's a terminal state");

            case Transition(var f, var t) when f == ApplicationStatus.OFFERED && t != ApplicationStatus.WITHDRAWN ->
                new StatusTransitionResult.Rejected(f, t, "An offered application can only move to WITHDRAWN");

            case Transition(var f, var t) -> new StatusTransitionResult.Accepted(f, t);
        };
    }

    private JobApplication findOwned(Long userId, Long applicationId) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        if (!application.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Application not found: " + applicationId);
        }
        return application;
    }

    private Response toResponse(JobApplication a) {
        return new Response(
                a.getId(),
                a.getCompany().getName(),
                a.getJobTitle(),
                a.getStatus(),
                a.getAppliedDate(),
                a.getSource(),
                a.getSalaryMin(),
                a.getSalaryMax(),
                a.getNotes(),
                a.getJobPostingUrl(),
                a.getCreatedAt(),
                a.getUpdatedAt(),
                a.getStageHistory().stream()
                        .map(h -> new StageHistoryEntry(h.getStatus(), h.getChangedAt(), h.getNote()))
                        .toList()
        );
    }

    private Summary toSummary(JobApplication a) {
        return new Summary(
                a.getId(),
                a.getCompany().getName(),
                a.getJobTitle(),
                a.getStatus(),
                a.getAppliedDate(),
                a.getUpdatedAt()
        );
    }
}