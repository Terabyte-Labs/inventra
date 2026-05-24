package mx.terabyte.labs.inventra.inventory.receiving;

import lombok.RequiredArgsConstructor;
import mx.terabyte.labs.inventra.auth.CurrentUserService;
import mx.terabyte.labs.inventra.auth.user.UserEntity;
import mx.terabyte.labs.inventra.catalog.product.ProductEntity;
import mx.terabyte.labs.inventra.catalog.product.ProductRepository;
import mx.terabyte.labs.inventra.catalog.product.barcode.ProductBarcodeEntity;
import mx.terabyte.labs.inventra.catalog.product.barcode.ProductBarcodeRepository;
import mx.terabyte.labs.inventra.catalog.supplier.SupplierEntity;
import mx.terabyte.labs.inventra.catalog.supplier.SupplierRepository;
import mx.terabyte.labs.inventra.common.enums.MovementType;
import mx.terabyte.labs.inventra.common.exception.BusinessException;
import mx.terabyte.labs.inventra.inventory.lot.ProductLotEntity;
import mx.terabyte.labs.inventra.inventory.lot.ProductLotRepository;
import mx.terabyte.labs.inventra.inventory.movement.InventoryMovementEntity;
import mx.terabyte.labs.inventra.inventory.movement.InventoryMovementRepository;
import mx.terabyte.labs.inventra.inventory.receiving.dto.ReceiveMaterialRequest;
import mx.terabyte.labs.inventra.inventory.receiving.dto.ReceiveMaterialResponse;
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
public class MaterialReceivingService {

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductLotRepository productLotRepository;
    private final ProductBarcodeRepository productBarcodeRepository;
    private final StockBalanceRepository stockBalanceRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public ReceiveMaterialResponse receive(ReceiveMaterialRequest request) {

        ProductEntity product = productRepository.findBySku(request.sku())
                .orElseThrow(() -> new BusinessException(
                        "PRODUCT_NOT_FOUND",
                        "Product not found for SKU: " + request.sku()
                ));

        SupplierEntity supplier = supplierRepository.findByName(request.supplierName())
                .orElseGet(() -> createSupplier(request.supplierName()));

        WarehouseEntity warehouse = warehouseRepository.findByCode(request.warehouseCode())
                .orElseThrow(() -> new BusinessException(
                        "WAREHOUSE_NOT_FOUND",
                        "Warehouse not found for code: " + request.warehouseCode()
                ));

        productLotRepository.findByProductIdAndLotNumber(
                product.getId(),
                request.lotNumber()
        ).ifPresent(existing -> {
            throw new BusinessException(
                    "LOT_ALREADY_EXISTS",
                    "Lot already exists for product SKU: " + request.sku()
            );
        });

        ProductLotEntity lot = createLot(
                request,
                product,
                supplier
        );

        if (request.barcode() != null && !request.barcode().isBlank()) {
            createBarcode(product, request.barcode());
        }

        StockBalanceEntity stock = stockBalanceRepository
                .findByProductIdAndWarehouseId(product.getId(), warehouse.getId())
                .orElseGet(() -> createStock(product, warehouse));

        BigDecimal before = stock.getQuantity();
        BigDecimal after = before.add(request.quantity());

        stock.setQuantity(after);
        stock.setUpdatedAt(LocalDateTime.now());

        stockBalanceRepository.save(stock);

        UserEntity currentUSer = currentUserService.getCurrentUser();

        InventoryMovementEntity movement = new InventoryMovementEntity();

        movement.setId(UUID.randomUUID());
        movement.setProduct(product);
        movement.setWarehouse(warehouse);
        movement.setProductLot(lot);
        movement.setMovementType(MovementType.STOCK_IN);
        movement.setQuantity(request.quantity());
        movement.setBeforeQuantity(before);
        movement.setAfterQuantity(after);
        movement.setUnitPrice(request.unitPrice());
        movement.setStorageLocation(request.storageLocation());
        movement.setNotes(request.notes());
        movement.setCreatedAt(LocalDateTime.now());
        movement.setCreatedBy(currentUSer);

        inventoryMovementRepository.save(movement);

        return new ReceiveMaterialResponse(
                movement.getId(),
                product.getSku(),
                product.getName(),
                lot.getLotNumber(),
                request.quantity(),
                before,
                after,
                warehouse.getCode()
        );
    }

    private SupplierEntity createSupplier(String name) {

        SupplierEntity supplier = new SupplierEntity();

        supplier.setId(UUID.randomUUID());
        supplier.setName(name);
        supplier.setActive(true);
        supplier.setCreatedAt(LocalDateTime.now());

        return supplierRepository.save(supplier);
    }

    private ProductLotEntity createLot(
            ReceiveMaterialRequest request,
            ProductEntity product,
            SupplierEntity supplier
    ) {

        ProductLotEntity lot = new ProductLotEntity();

        lot.setId(UUID.randomUUID());
        lot.setProduct(product);
        lot.setSupplier(supplier);
        lot.setLotNumber(request.lotNumber());
        lot.setSerialNumber(request.serialNumber());
        lot.setPresentationQuantity(request.presentationQuantity());
        lot.setPresentationUnit(request.presentationUnit());
        lot.setUnitPrice(request.unitPrice());
        lot.setReceivedAt(
                request.receivedAt() != null
                        ? request.receivedAt()
                        : LocalDateTime.now()
        );
        lot.setCreatedAt(LocalDateTime.now());

        return productLotRepository.save(lot);
    }

    private void createBarcode(ProductEntity product, String barcode) {

        productBarcodeRepository.findByBarcode(barcode)
                .ifPresent(existing -> {
                    throw new BusinessException(
                            "BARCODE_ALREADY_EXISTS",
                            "Barcode already exists: " + barcode
                    );
                });

        ProductBarcodeEntity entity = new ProductBarcodeEntity();

        entity.setId(UUID.randomUUID());
        entity.setProduct(product);
        entity.setBarcode(barcode);
        entity.setActive(true);
        entity.setCreatedAt(LocalDateTime.now());

        productBarcodeRepository.save(entity);
    }

    private StockBalanceEntity createStock(
            ProductEntity product,
            WarehouseEntity warehouse
    ) {

        StockBalanceEntity stock = new StockBalanceEntity();

        stock.setId(UUID.randomUUID());
        stock.setProduct(product);
        stock.setWarehouse(warehouse);
        stock.setQuantity(BigDecimal.ZERO);
        stock.setCreatedAt(LocalDateTime.now());

        return stockBalanceRepository.save(stock);
    }
}