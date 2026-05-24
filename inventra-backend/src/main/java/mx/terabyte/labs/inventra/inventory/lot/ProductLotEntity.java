package mx.terabyte.labs.inventra.inventory.lot;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mx.terabyte.labs.inventra.catalog.product.ProductEntity;
import mx.terabyte.labs.inventra.catalog.supplier.SupplierEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "product_lots",
        schema = "inventory",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_product_lot",
                        columnNames = {"product_id", "lot_number"}
                )
        }
)
public class ProductLotEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private SupplierEntity supplier;

    @Column(name = "lot_number", nullable = false, length = 120)
    private String lotNumber;

    @Column(name = "serial_number", length = 120)
    private String serialNumber;

    @Column(name = "presentation_quantity", precision = 18, scale = 4)
    private BigDecimal presentationQuantity;

    @Column(name = "presentation_unit", length = 20)
    private String presentationUnit;

    @Column(name = "unit_price", precision = 18, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}