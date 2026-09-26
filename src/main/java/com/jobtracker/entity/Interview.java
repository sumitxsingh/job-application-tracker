package com.jobtracker.entity;

import com.jobtracker.enums.InterviewType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "interviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id")
    private JobApplication application;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewType type;

    private Integer round;

    @Column(nullable = false)
    private Instant scheduledAt;

    private String interviewerName;

    @Column(length = 2000)
    private String feedback;
}