package com.jobtracker.service;

import com.jobtracker.enums.ApplicationStatus;

public sealed interface StatusTransitionResult
        permits StatusTransitionResult.Accepted, StatusTransitionResult.Rejected {

    record Accepted(ApplicationStatus from, ApplicationStatus to) implements StatusTransitionResult {}

    record Rejected(ApplicationStatus from, ApplicationStatus attempted, String reason) implements StatusTransitionResult {}
}