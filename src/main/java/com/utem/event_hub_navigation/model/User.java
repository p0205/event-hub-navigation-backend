package com.utem.event_hub_navigation.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
// @Inheritance(strategy = InheritanceType.SINGLE_TABLE) // Use single table inheritance
// @DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING)
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor


public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // @NotBlank(message = "Name is required.")
    private String name;

    // @Schema(example="example@email.com")
    // @Email
    @Column(unique = true)
    private String email;

    // @Schema(example = "012-345-5678",minLength = 1, maxLength = 15)
    @Column(length = 15, unique = true)
    private String phoneNo;

    private Character gender; 

    private String passwordHash;

    private String faculty;

    private String course;
    private String year;

    private String role; 

    @CreatedDate
    private LocalDate createdAt;

    @LastModifiedDate
    private LocalDateTime lastUpdatedAt;

}