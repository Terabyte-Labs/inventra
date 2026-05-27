package mx.terabyte.labs.inventra.inventory.movement;

import mx.terabyte.labs.inventra.inventory.movement.dto.InventoryMovementResponse;
import org.springframework.stereotype.Component;

@Component
public class InventoryMovementMapper {

    public InventoryMovementResponse toResponse(InventoryMovementEntity movement) {
        return new InventoryMovementResponse(
                movement.getId(),

                movement.getProduct().getSku(),
                movement.getProduct().getName(),
                movement.getProductLot() != null
                        ? movement.getProductLot().getLotNumber()
                        : null,
                movement.getWarehouse().getCode(),

                movement.getMovementType(),

                movement.getRequestedQuantity(),
                movement.getRequestedUnitOfMeasure().getCode(),
                movement.getRequestedUnitOfMeasure().getName(),

                movement.getQuantity(),
                movement.getUnitOfMeasure().getCode(),
                movement.getUnitOfMeasure().getName(),

                movement.getBeforeQuantity(),
                movement.getAfterQuantity(),

                movement.getUnitPrice(),
                movement.getStorageLocation(),
                movement.getNotes(),

                movement.getCreatedBy() != null
                        ? movement.getCreatedBy().getUsername()
                        : null,
                movement.getCreatedAt()
        );
    }
}