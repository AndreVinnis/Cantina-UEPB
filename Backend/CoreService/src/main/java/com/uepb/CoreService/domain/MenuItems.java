package com.uepb.CoreService.domain;

import com.uepb.CoreService.enums.Category;
import com.uepb.CoreService.enums.AvailabilityMode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "menu_items")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MenuItems {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cafeteria_id", nullable = false)
    private Cafeteria cafeteria;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 150)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    @Builder.Default
    private boolean availability = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_mode", nullable = false)
    private AvailabilityMode availabilityMode;

    private Integer stock;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
