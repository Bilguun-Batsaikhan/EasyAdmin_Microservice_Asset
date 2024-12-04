package com.example.table.model;

import com.example.table.enumeration.AssetStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "assets")
@Data
@NoArgsConstructor
@AllArgsConstructor

//TODO: add validations not blank etc...
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String modelName;

    @Column(name = "type")
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AssetStatus status;

    @Column(name = "cost")
    private BigDecimal cost;

    @Column(name = "user_id")
    private Long userID;
}
