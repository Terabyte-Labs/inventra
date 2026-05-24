package mx.terabyte.labs.inventra.manufacturing.formula;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mx.terabyte.labs.inventra.catalog.product.ProductEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "formulas", schema = "manufacturing")
public class FormulaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 80)
    private String code;

    @Column(nullable = false, length = 180)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(name = "output_quantity", nullable = false, precision = 18, scale = 4)
    private BigDecimal outputQuantity;

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}