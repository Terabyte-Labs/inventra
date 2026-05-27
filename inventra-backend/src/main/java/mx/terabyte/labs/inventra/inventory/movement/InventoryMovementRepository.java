package mx.terabyte.labs.inventra.inventory.movement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface InventoryMovementRepository
        extends JpaRepository<InventoryMovementEntity, UUID> {
    List<InventoryMovementEntity> findAllByOrderByCreatedAtDesc();

    @Query("""
                SELECT m
                FROM InventoryMovementEntity m
                JOIN FETCH m.product p
                JOIN FETCH m.warehouse w
                JOIN FETCH m.requestedUnitOfMeasure requestedUnit
                JOIN FETCH m.unitOfMeasure inventoryUnit
                LEFT JOIN FETCH m.productLot l
                LEFT JOIN FETCH m.createdBy u
                ORDER BY m.createdAt DESC
            """)
    List<InventoryMovementEntity> findAllWithDetails();

    @Query("""
                SELECT DISTINCT m
                FROM InventoryMovementEntity m
                JOIN FETCH m.product p
                JOIN FETCH m.warehouse w
                JOIN FETCH m.requestedUnitOfMeasure requestedUnit
                JOIN FETCH m.unitOfMeasure inventoryUnit
                LEFT JOIN FETCH m.productLot l
                LEFT JOIN FETCH m.createdBy u
                WHERE m.id IN (
                    SELECT input.inventoryMovement.id
                    FROM ManufacturingOrderInputEntity input
                    WHERE input.manufacturingOrder.id = :manufacturingOrderId
                      AND input.inventoryMovement IS NOT NULL
                )
                OR m.id IN (
                    SELECT output.inventoryMovement.id
                    FROM ManufacturingOrderOutputEntity output
                    WHERE output.manufacturingOrder.id = :manufacturingOrderId
                      AND output.inventoryMovement IS NOT NULL
                )
                ORDER BY m.createdAt ASC
            """)
    List<InventoryMovementEntity> findByManufacturingOrderIdWithDetails(
            @Param("manufacturingOrderId") UUID manufacturingOrderId
    );
}