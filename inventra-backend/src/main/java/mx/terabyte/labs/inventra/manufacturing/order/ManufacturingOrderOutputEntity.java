package mx.terabyte.labs.inventra.manufacturing.order;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mx.terabyte.labs.inventra.catalog.product.ProductEntity;
import mx.terabyte.labs.inventra.inventory.movement.InventoryMovementEntity;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "manufacturing_order_outputs", schema = "manufacturing")
public class ManufacturingOrderOutputEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manufacturing_order_id", nullable = false)
    private ManufacturingOrderEntity manufacturingOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_movement_id")
    private InventoryMovementEntity inventoryMovement;
}