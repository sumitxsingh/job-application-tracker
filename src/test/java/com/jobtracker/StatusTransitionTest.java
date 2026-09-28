package com.jobtracker;

import com.jobtracker.dto.JobApplicationDtos.Response;
import com.jobtracker.dto.JobApplicationDtos.StatusUpdateRequest;
import com.jobtracker.entity.Company;
import com.jobtracker.entity.JobApplication;
import com.jobtracker.entity.User;
import com.jobtracker.enums.ApplicationStatus;
import com.jobtracker.exception.InvalidTransitionException;
import com.jobtracker.exception.ResourceNotFoundException;
import com.jobtracker.repository.CompanyRepository;
import com.jobtracker.repository.JobApplicationRepository;
import com.jobtracker.service.JobApplicationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusTransitionTest {

    private static final Long USER_ID = 1L;

    @Mock
    private JobApplicationRepository applicationRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private JobApplicationService service;

    @Test
    void validTransition_updatesStatusAndAppendsHistory() {
        when(applicationRepository.findById(11L))
                .thenReturn(Optional.of(application(11L, USER_ID, ApplicationStatus.APPLIED)));
        when(applicationRepository.save(any(JobApplication.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Response response = service.updateStatus(USER_ID, 11L,
                new StatusUpdateRequest(ApplicationStatus.SCREENING, "Recruiter reached out"));

        assertThat(response.status()).isEqualTo(ApplicationStatus.SCREENING);
        assertThat(response.stageHistory()).hasSize(1);
        assertThat(response.stageHistory().get(0).note()).isEqualTo("Recruiter reached out");
    }

    @Test
    void rejectedApplication_isTerminal() {
        assertRejected(ApplicationStatus.REJECTED, ApplicationStatus.INTERVIEWING, "terminal state");
    }

    @Test
    void withdrawnApplication_isTerminal() {
        assertRejected(ApplicationStatus.WITHDRAWN, ApplicationStatus.APPLIED, "terminal state");
    }

    @Test
    void sameStatus_isRejected() {
        assertRejected(ApplicationStatus.APPLIED, ApplicationStatus.APPLIED, "already in status");
    }

    @Test
    void offeredApplication_canOnlyMoveToWithdrawn() {
        assertRejected(ApplicationStatus.OFFERED, ApplicationStatus.INTERVIEWING, "only move to WITHDRAWN");
    }

    @Test
    void offeredApplication_canBeWithdrawn() {
        when(applicationRepository.findById(12L))
                .thenReturn(Optional.of(application(12L, USER_ID, ApplicationStatus.OFFERED)));
        when(applicationRepository.save(any(JobApplication.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Response response = service.updateStatus(USER_ID, 12L,
                new StatusUpdateRequest(ApplicationStatus.WITHDRAWN, null));

        assertThat(response.status()).isEqualTo(ApplicationStatus.WITHDRAWN);
    }

    @Test
    void otherUsersApplication_looksLikeNotFound() {
        when(applicationRepository.findById(13L))
                .thenReturn(Optional.of(application(13L, 2L, ApplicationStatus.APPLIED)));

        assertThatThrownBy(() -> service.updateStatus(USER_ID, 13L,
                new StatusUpdateRequest(ApplicationStatus.SCREENING, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private void assertRejected(ApplicationStatus from, ApplicationStatus to, String messagePart) {
        when(applicationRepository.findById(10L))
                .thenReturn(Optional.of(application(10L, USER_ID, from)));

        assertThatThrownBy(() -> service.updateStatus(USER_ID, 10L, new StatusUpdateRequest(to, null)))
                .isInstanceOf(InvalidTransitionException.class)
                .hasMessageContaining(messagePart);
    }

    private JobApplication application(Long id, Long ownerId, ApplicationStatus status) {
        return JobApplication.builder()
                .id(id)
                .user(User.builder().id(ownerId).build())
                .company(Company.builder().id(1L).name("Acme").build())
                .status(status)
                .build();
    }
}