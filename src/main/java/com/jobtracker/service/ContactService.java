package com.jobtracker.service;

import com.jobtracker.dto.ContactDtos.*;
import com.jobtracker.entity.Company;
import com.jobtracker.entity.Contact;
import com.jobtracker.exception.ResourceNotFoundException;
import com.jobtracker.repository.CompanyRepository;
import com.jobtracker.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;
    private final CompanyRepository companyRepository;

    @Transactional
    public Response create(Long companyId, CreateRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + companyId));

        Contact contact = Contact.builder()
                .company(company)
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .roleTitle(request.roleTitle())
                .build();

        return toResponse(contactRepository.save(contact));
    }

    public List<Response> listForCompany(Long companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new ResourceNotFoundException("Company not found: " + companyId);
        }
        return contactRepository.findByCompanyId(companyId).stream()
                .map(this::toResponse)
                .toList();
    }

    private Response toResponse(Contact c) {
        return new Response(
                c.getId(),
                c.getCompany().getId(),
                c.getName(),
                c.getEmail(),
                c.getPhone(),
                c.getRoleTitle()
        );
    }
}