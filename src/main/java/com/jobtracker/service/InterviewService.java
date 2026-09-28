package com.jobtracker.service;

import com.jobtracker.dto.InterviewDtos.*;
import com.jobtracker.entity.Interview;
import com.jobtracker.entity.JobApplication;
import com.jobtracker.exception.ResourceNotFoundException;
import com.jobtracker.repository.InterviewRepository;
import com.jobtracker.repository.JobApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final JobApplicationRepository applicationRepository;

    @Transactional
    public Response schedule(Long userId, Long applicationId, ScheduleRequest request) {
        JobApplication application = findOwnedApplication(userId, applicationId);

        Interview interview = Interview.builder()
                .application(application)
                .type(request.type())
                .round(request.round())
                .scheduledAt(request.scheduledAt())
                .interviewerName(request.interviewerName())
                .build();

        return toResponse(interviewRepository.save(interview));
    }

    public List<Response> listForApplication(Long userId, Long applicationId) {
        findOwnedApplication(userId, applicationId); // ownership check
        return interviewRepository.findByApplicationIdOrderByScheduledAtAsc(applicationId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public Response updateFeedback(Long userId, Long interviewId, FeedbackRequest request) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found: " + interviewId));

        if (!interview.getApplication().getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Interview not found: " + interviewId);
        }

        interview.setFeedback(request.feedback());
        return toResponse(interviewRepository.save(interview));
    }

    private JobApplication findOwnedApplication(Long userId, Long applicationId) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        if (!application.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Application not found: " + applicationId);
        }
        return application;
    }

    private Response toResponse(Interview i) {
        return new Response(
                i.getId(),
                i.getApplication().getId(),
                i.getType(),
                i.getRound(),
                i.getScheduledAt(),
                i.getInterviewerName(),
                i.getFeedback()
        );
    }
}