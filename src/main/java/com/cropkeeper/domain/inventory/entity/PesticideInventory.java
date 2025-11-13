package com.cropkeeper.domain.inventory.entity;

import com.cropkeeper.domain.farm.entity.Farm;
import com.cropkeeper.domain.pest.entity.Pesticide;
import com.cropkeeper.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pesticide_inventory",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"farm_id", "pesticide_id"})
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PesticideInventory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pesticide_inventory_id")
    private Long pesticideInventoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pesticide_id", nullable = false)
    private Pesticide pesticide;

    @Column(name = "quantity_bottle", nullable = false)
    private Long quantityBottle;
}
