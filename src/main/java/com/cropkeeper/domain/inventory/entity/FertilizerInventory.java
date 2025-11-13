package com.cropkeeper.domain.inventory.entity;

import com.cropkeeper.domain.farm.entity.Farm;
import com.cropkeeper.domain.fertilizer.entity.Fertilizer;
import com.cropkeeper.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fertilizer_inventory",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"farm_id", "fertilizer_id"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FertilizerInventory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fertilizer_inventory_id")
    private Long fertilizerInventoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fertilizer_id", nullable = false)
    private Fertilizer fertilizer;

    @Column(name = "quantity_bag", nullable = false)
    private Long quantityBag;
}
