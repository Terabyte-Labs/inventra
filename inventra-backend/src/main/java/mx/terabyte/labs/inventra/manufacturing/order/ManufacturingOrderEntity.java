package mx.terabyte.labs.inventra.manufacturing.order;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mx.terabyte.labs.inventra.catalog.product.ProductEntity;
import mx.terabyte.labs.inventra.common.enums.ManufacturingOrderStatus;
import mx.terabyte.labs.inventra.manufacturing.formula.FormulaEntity;
import mx.terabyte.labs.inventra.inventory.warehouse.WarehouseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "manufacturing_orders", schema = "manufacturing")
public class ManufacturingOrderEntity {

    @Id
    private UUID id;

    @Column(name = "order_number", nullable = false, unique = true, length = 80)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formula_id", nullable = false)
    private FormulaEntity formula;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(name = "planned_quantity", nullable = false, precision = 18, scale = 4)
    private BigDecimal plannedQuantity;

    @Column(name = "actual_quantity", precision = 18, scale = 4)
    private BigDecimal actualQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private WarehouseEntity warehouse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ManufacturingOrderStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}