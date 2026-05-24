package mx.terabyte.labs.inventra.inventory.movement;

import mx.terabyte.labs.inventra.inventory.movement.dto.InventoryMovementResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface InventoryMovementRepository
        extends JpaRepository<InventoryMovementEntity, UUID> {
    List<InventoryMovementEntity> findAllByOrderByCreatedAtDesc();
    @Query("""
    SELECT new mx.terabyte.labs.inventra.inventory.movement.dto.InventoryMovementResponse(
        m.id,
        p.sku,
        p.name,
        l.lotNumber,
        w.code,
        m.movementType,
        m.quantity,
        m.beforeQuantity,
        m.afterQuantity,
        m.unitPrice,
        m.storageLocation,
        m.notes,
        u.username,
        m.createdAt
    )
    FROM InventoryMovementEntity m
    JOIN m.product p
    JOIN m.warehouse w
    LEFT JOIN m.productLot l
    LEFT JOIN m.createdBy u
    ORDER BY m.createdAt DESC
""")
    List<InventoryMovementResponse> findAllProjected();
}