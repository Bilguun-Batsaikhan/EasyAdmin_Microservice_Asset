package com.certimeter.asset.model;

import com.certimeter.asset.enumeration.AssetAction;
import com.certimeter.asset.enumeration.AssetStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Table(name = "asset_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class AssetHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long assetId;

    @Column(nullable = false)
    private Long adminId; // ID of the admin making the change

    private Long userId; // The user this asset is assigned to (if any)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetAction action;

    @Column(nullable = false)
    private LocalDateTime date;

    private String comment;

}

