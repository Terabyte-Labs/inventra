package mx.terabyte.labs.inventra.inventory.movement;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mx.terabyte.labs.inventra.auth.user.UserEntity;
import mx.terabyte.labs.inventra.catalog.product.ProductEntity;
import mx.terabyte.labs.inventra.common.enums.MovementType;
import mx.terabyte.labs.inventra.common.enums.ReferenceType;
import mx.terabyte.labs.inventra.inventory.lot.ProductLotEntity;
import mx.terabyte.labs.inventra.inventory.warehouse.WarehouseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "inventory_movements", schema = "inventory")
public class InventoryMovementEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private WarehouseEntity warehouse;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 40)
    private MovementType movementType;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal quantity;

    @Column(name = "before_quantity", nullable = false, precision = 18, scale = 4)
    private BigDecimal beforeQuantity;

    @Column(name = "after_quantity", nullable = false, precision = 18, scale = 4)
    private BigDecimal afterQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", length = 60)
    private ReferenceType referenceType;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_lot_id")
    private ProductLotEntity productLot;

    @Column(name = "unit_price", precision = 18, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "storage_location", length = 120)
    private String storageLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private UserEntity createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}