package com.jobtracker.repository;

import com.jobtracker.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface JobApplicationRepository
        extends JpaRepository<JobApplication, Long>, JpaSpecificationExecutor<JobApplication> {

    List<JobApplication> findByUserId(Long userId);

    @Query("""
           select a from JobApplication a
           where a.user.id = :userId
             and a.status not in ('OFFERED', 'REJECTED', 'WITHDRAWN')
             and a.updatedAt < :threshold
           """)
    List<JobApplication> findStaleApplications(@Param("userId") Long userId, @Param("threshold") Instant threshold);

    @Query("""
           select a from JobApplication a
           join fetch a.user
           join fetch a.company
           where a.status not in ('OFFERED', 'REJECTED', 'WITHDRAWN')
             and a.updatedAt < :threshold
           """)
    List<JobApplication> findAllStale(@Param("threshold") Instant threshold);

    long countByUserIdAndStatus(Long userId, com.jobtracker.enums.ApplicationStatus status);
}