package com.jobtracker.controller;

import com.jobtracker.dto.InterviewDtos.*;
import com.jobtracker.entity.User;
import com.jobtracker.service.InterviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Interviews", description = "Schedule and track interview rounds for an application")
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping("/applications/{applicationId}/interviews")
    public ResponseEntity<Response> schedule(@AuthenticationPrincipal User user,
                                              @PathVariable Long applicationId,
                                              @Valid @RequestBody ScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(interviewService.schedule(user.getId(), applicationId, request));
    }

    @GetMapping("/applications/{applicationId}/interviews")
    public ResponseEntity<List<Response>> list(@AuthenticationPrincipal User user,
                                                @PathVariable Long applicationId) {
        return ResponseEntity.ok(interviewService.listForApplication(user.getId(), applicationId));
    }

    @PatchMapping("/interviews/{interviewId}/feedback")
    public ResponseEntity<Response> updateFeedback(@AuthenticationPrincipal User user,
                                                    @PathVariable Long interviewId,
                                                    @RequestBody FeedbackRequest request) {
        return ResponseEntity.ok(interviewService.updateFeedback(user.getId(), interviewId, request));
    }
}