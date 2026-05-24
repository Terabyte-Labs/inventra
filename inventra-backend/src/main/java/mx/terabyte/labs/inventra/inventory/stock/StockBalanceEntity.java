package mx.terabyte.labs.inventra.inventory.stock;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mx.terabyte.labs.inventra.catalog.product.ProductEntity;
import mx.terabyte.labs.inventra.inventory.warehouse.WarehouseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "stock_balances",
        schema = "inventory",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_stock_product_warehouse",
                        columnNames = {"product_id", "warehouse_id"}
                )
        }
)
public class StockBalanceEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private WarehouseEntity warehouse;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal quantity;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}