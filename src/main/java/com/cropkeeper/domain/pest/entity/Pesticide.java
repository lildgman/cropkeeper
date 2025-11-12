package com.cropkeeper.domain.pest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pesticide")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Pesticide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pesticide_id")
    private Long pesticideId;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "manufacturer", length = 100)
    private String manufacturer;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private PesticideType type;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
