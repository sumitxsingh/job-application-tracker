package com.jobtracker.controller;

import com.jobtracker.dto.ContactDtos.*;
import com.jobtracker.service.ContactService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies/{companyId}/contacts")
@RequiredArgsConstructor
@Tag(name = "Companies", description = "Companies referenced by job applications, and their contacts")
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<Response> create(@PathVariable Long companyId,
                                            @Valid @RequestBody CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contactService.create(companyId, request));
    }

    @GetMapping
    public ResponseEntity<List<Response>> list(@PathVariable Long companyId) {
        return ResponseEntity.ok(contactService.listForCompany(companyId));
    }
}