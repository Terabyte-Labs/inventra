package mx.terabyte.labs.inventra.manufacturing.order;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mx.terabyte.labs.inventra.catalog.product.ProductEntity;
import mx.terabyte.labs.inventra.catalog.unit.UnitOfMeasureEntity;
import mx.terabyte.labs.inventra.inventory.movement.InventoryMovementEntity;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "manufacturing_order_inputs", schema = "manufacturing")
public class ManufacturingOrderInputEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manufacturing_order_id", nullable = false)
    private ManufacturingOrderEntity manufacturingOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(name = "planned_quantity", nullable = false, precision = 18, scale = 4)
    private BigDecimal plannedQuantity;

    @Column(name = "actual_quantity", precision = 18, scale = 4)
    private BigDecimal actualQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_movement_id")
    private InventoryMovementEntity inventoryMovement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planned_unit_of_measure_id", nullable = false)
    private UnitOfMeasureEntity plannedUnitOfMeasure;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actual_unit_of_measure_id", nullable = false)
    private UnitOfMeasureEntity actualUnitOfMeasure;
}