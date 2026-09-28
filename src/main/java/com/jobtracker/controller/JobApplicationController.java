package com.jobtracker.controller;

import com.jobtracker.dto.JobApplicationDtos.*;
import com.jobtracker.dto.PageResponse;
import com.jobtracker.entity.User;
import com.jobtracker.enums.ApplicationStatus;
import com.jobtracker.service.JobApplicationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Tag(name = "Job Applications", description = "Create, list and progress job applications")
public class JobApplicationController {

    private final JobApplicationService applicationService;

    @PostMapping
    public ResponseEntity<Response> create(@AuthenticationPrincipal User user,
                                            @Valid @RequestBody CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationService.create(user, request));
    }

    @GetMapping
    public ResponseEntity<PageResponse<Summary>> list(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                applicationService.search(user.getId(), status, company, from, to, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> get(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getOwned(user.getId(), id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Response> updateStatus(@AuthenticationPrincipal User user,
                                                  @PathVariable Long id,
                                                  @Valid @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(applicationService.updateStatus(user.getId(), id, request));
    }
}