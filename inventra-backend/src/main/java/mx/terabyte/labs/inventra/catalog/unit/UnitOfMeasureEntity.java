package mx.terabyte.labs.inventra.catalog.unit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mx.terabyte.labs.inventra.catalog.product.ProductEntity;
import mx.terabyte.labs.inventra.common.enums.UnitGroup;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "units_of_measure", schema = "catalog")
public class UnitOfMeasureEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_group", nullable = false, length = 30)
    private UnitGroup unitGroup;

    @Column(name = "conversion_factor_to_base", nullable = false, precision = 19, scale = 6)
    private BigDecimal conversionFactorToBase;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}