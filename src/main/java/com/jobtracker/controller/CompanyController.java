package com.jobtracker.controller;

import com.jobtracker.dto.CompanyDtos.Response;
import com.jobtracker.entity.Company;
import com.jobtracker.exception.ResourceNotFoundException;
import com.jobtracker.repository.CompanyRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Tag(name = "Companies", description = "Companies referenced by job applications, and their contacts")
public class CompanyController {

    private final CompanyRepository companyRepository;

    @GetMapping
    public List<Response> list() {
        return companyRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public Response get(@PathVariable Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + id));
        return toResponse(company);
    }

    private Response toResponse(Company c) {
        return new Response(c.getId(), c.getName(), c.getWebsite(), c.getIndustry());
    }
}