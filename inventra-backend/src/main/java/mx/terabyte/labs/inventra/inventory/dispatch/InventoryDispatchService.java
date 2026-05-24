package mx.terabyte.labs.inventra.inventory.dispatch;

import lombok.RequiredArgsConstructor;
import mx.terabyte.labs.inventra.auth.CurrentUserService;
import mx.terabyte.labs.inventra.auth.user.UserEntity;
import mx.terabyte.labs.inventra.catalog.product.ProductEntity;
import mx.terabyte.labs.inventra.catalog.product.ProductRepository;
import mx.terabyte.labs.inventra.common.enums.MovementType;
import mx.terabyte.labs.inventra.common.exception.BusinessException;
import mx.terabyte.labs.inventra.inventory.dispatch.dto.DispatchInventoryRequest;
import mx.terabyte.labs.inventra.inventory.dispatch.dto.DispatchInventoryResponse;
import mx.terabyte.labs.inventra.inventory.movement.InventoryMovementEntity;
import mx.terabyte.labs.inventra.inventory.movement.InventoryMovementRepository;
import mx.terabyte.labs.inventra.inventory.stock.StockBalanceEntity;
import mx.terabyte.labs.inventra.inventory.stock.StockBalanceRepository;
import mx.terabyte.labs.inventra.inventory.warehouse.WarehouseEntity;
import mx.terabyte.labs.inventra.inventory.warehouse.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryDispatchService {

    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockBalanceRepository stockBalanceRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public DispatchInventoryResponse dispatch(
            DispatchInventoryRequest request
    ) {

        ProductEntity product = productRepository.findBySku(request.sku())
                .orElseThrow(() -> new BusinessException(
                        "PRODUCT_NOT_FOUND",
                        "Product not found for SKU: " + request.sku()
                ));

        WarehouseEntity warehouse = warehouseRepository.findByCode(request.warehouseCode())
                .orElseThrow(() -> new BusinessException(
                        "WAREHOUSE_NOT_FOUND",
                        "Warehouse not found for code: " + request.warehouseCode()
                ));

        StockBalanceEntity stock = stockBalanceRepository
                .findByProductIdAndWarehouseId(
                        product.getId(),
                        warehouse.getId()
                )
                .orElseThrow(() -> new BusinessException(
                        "STOCK_NOT_FOUND",
                        "Stock not found"
                ));

        BigDecimal before = stock.getQuantity();

        if (before.compareTo(request.quantity()) < 0) {
            throw new BusinessException(
                    "INSUFFICIENT_STOCK",
                    "Insufficient stock for SKU: " + request.sku()
            );
        }

        BigDecimal after = before.subtract(request.quantity());

        stock.setQuantity(after);
        stock.setUpdatedAt(LocalDateTime.now());

        stockBalanceRepository.save(stock);

        UserEntity currentUser = currentUserService.getCurrentUser();
        InventoryMovementEntity movement = new InventoryMovementEntity();

        movement.setId(UUID.randomUUID());
        movement.setProduct(product);
        movement.setWarehouse(warehouse);
        movement.setMovementType(MovementType.STOCK_OUT);
        movement.setQuantity(request.quantity());
        movement.setBeforeQuantity(before);
        movement.setAfterQuantity(after);
        movement.setNotes(request.notes());
        movement.setCreatedAt(LocalDateTime.now());
        movement.setCreatedBy(currentUser);

        inventoryMovementRepository.save(movement);

        return new DispatchInventoryResponse(
                movement.getId(),
                product.getSku(),
                product.getName(),
                warehouse.getCode(),
                MovementType.STOCK_OUT,
                request.quantity(),
                before,
                after,
                request.reason()
        );
    }
}