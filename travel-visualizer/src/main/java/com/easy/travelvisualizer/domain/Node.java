package com.easy.travelvisualizer.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Node {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String currentCity;
    private BigDecimal price;
    private Instant startMoment;
    private Instant endMoment;
    private String currency;
    private String transportCompanyName;
    private String departurePlace;
    private String arrivalPlace;
    private String transportType;
    @OneToOne
    @JoinColumn(name = "previous_node")
    private Node previousNode;

}
