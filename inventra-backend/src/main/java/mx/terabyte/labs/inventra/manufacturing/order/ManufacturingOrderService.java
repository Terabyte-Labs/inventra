package mx.terabyte.labs.inventra.manufacturing.order;

import lombok.RequiredArgsConstructor;
import mx.terabyte.labs.inventra.catalog.product.ProductRepository;
import mx.terabyte.labs.inventra.common.enums.ManufacturingOrderStatus;
import mx.terabyte.labs.inventra.common.enums.MovementType;
import mx.terabyte.labs.inventra.common.enums.ReferenceType;
import mx.terabyte.labs.inventra.common.exception.BusinessException;
import mx.terabyte.labs.inventra.inventory.lot.ProductLotEntity;
import mx.terabyte.labs.inventra.inventory.lot.ProductLotRepository;
import mx.terabyte.labs.inventra.inventory.movement.InventoryMovementEntity;
import mx.terabyte.labs.inventra.inventory.movement.InventoryMovementRepository;
import mx.terabyte.labs.inventra.inventory.stock.StockBalanceEntity;
import mx.terabyte.labs.inventra.inventory.stock.StockBalanceRepository;
import mx.terabyte.labs.inventra.inventory.warehouse.WarehouseEntity;
import mx.terabyte.labs.inventra.inventory.warehouse.WarehouseRepository;
import mx.terabyte.labs.inventra.manufacturing.formula.FormulaEntity;
import mx.terabyte.labs.inventra.manufacturing.formula.FormulaItemRepository;
import mx.terabyte.labs.inventra.manufacturing.formula.FormulaRepository;
import mx.terabyte.labs.inventra.manufacturing.order.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import mx.terabyte.labs.inventra.catalog.product.ProductEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManufacturingOrderService {

    private final ManufacturingOrderRepository manufacturingOrderRepository;
    private final ManufacturingOrderInputRepository manufacturingOrderInputRepository;
    private final FormulaRepository formulaRepository;
    private final WarehouseRepository warehouseRepository;
    private final FormulaItemRepository formulaItemRepository;
    private final StockBalanceRepository stockBalanceRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final ManufacturingOrderOutputRepository manufacturingOrderOutputRepository;
    private final ProductRepository productRepository;
    private final ProductLotRepository productLotRepository;


    @Transactional
    public CreateManufacturingOrderResponse create(
            CreateManufacturingOrderRequest request
    ) {
        manufacturingOrderRepository.findByOrderNumber(request.orderNumber())
                .ifPresent(existing -> {
                    throw new BusinessException(
                            "MANUFACTURING_ORDER_ALREADY_EXISTS",
                            "Manufacturing order already exists for order number: "
                                    + request.orderNumber()
                    );
                });

        FormulaEntity formula = formulaRepository
                .findByCodeAndVersion(
                        request.formulaCode(),
                        request.formulaVersion()
                )
                .orElseThrow(() -> new BusinessException(
                        "FORMULA_NOT_FOUND",
                        "Formula not found for code: "
                                + request.formulaCode()
                                + " and version: "
                                + request.formulaVersion()
                ));

        WarehouseEntity warehouse = warehouseRepository
                .findByCode(request.warehouseCode())
                .orElseThrow(() -> new BusinessException(
                        "WAREHOUSE_NOT_FOUND",
                        "Warehouse not found for code: "
                                + request.warehouseCode()
                ));

        ProductEntity outputProduct = formula.getProduct();

        ManufacturingOrderEntity order = new ManufacturingOrderEntity();

        order.setId(UUID.randomUUID());
        order.setOrderNumber(request.orderNumber());
        order.setFormula(formula);
        order.setProduct(outputProduct);
        order.setPlannedQuantity(request.plannedQuantity());
        order.setWarehouse(warehouse);
        order.setStatus(ManufacturingOrderStatus.DRAFT);
        order.setCreatedAt(LocalDateTime.now());

        manufacturingOrderRepository.save(order);

        return new CreateManufacturingOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                formula.getCode(),
                formula.getVersion(),
                outputProduct.getSku(),
                order.getPlannedQuantity(),
                order.getStatus()
        );
    }

    @Transactional
    public StartManufacturingOrderResponse start(String orderNumber) {

        ManufacturingOrderEntity order = manufacturingOrderRepository
                .findByOrderNumber(orderNumber)
                .orElseThrow(() -> new BusinessException(
                        "MANUFACTURING_ORDER_NOT_FOUND",
                        "Manufacturing order not found: " + orderNumber
                ));

        if (order.getStatus() != ManufacturingOrderStatus.DRAFT) {
            throw new BusinessException(
                    "INVALID_MANUFACTURING_ORDER_STATUS",
                    "Only DRAFT manufacturing orders can be started. Current status: "
                            + order.getStatus()
            );
        }

        order.setStatus(ManufacturingOrderStatus.IN_PROGRESS);
        order.setStartedAt(LocalDateTime.now());

        manufacturingOrderRepository.save(order);

        return new StartManufacturingOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getStartedAt()
        );
    }

    @Transactional
    public AddManufacturingInputResponse addInput(
            String orderNumber,
            AddManufacturingInputRequest request
    ) {

        ManufacturingOrderEntity order = manufacturingOrderRepository
                .findByOrderNumber(orderNumber)
                .orElseThrow(() -> new BusinessException(
                        "MANUFACTURING_ORDER_NOT_FOUND",
                        "Manufacturing order not found: " + orderNumber
                ));

        if (order.getStatus() != ManufacturingOrderStatus.IN_PROGRESS) {
            throw new BusinessException(
                    "INVALID_MANUFACTURING_ORDER_STATUS",
                    "Manufacturing order must be IN_PROGRESS"
            );
        }

        ProductEntity product = productRepository
                .findBySku(request.productSku())
                .orElseThrow(() -> new BusinessException(
                        "PRODUCT_NOT_FOUND",
                        "Product not found for SKU: " + request.productSku()
                ));

        ProductLotEntity lot = productLotRepository
                .findByProductIdAndLotNumber(
                        product.getId(),
                        request.lotNumber()
                )
                .orElseThrow(() -> new BusinessException(
                        "PRODUCT_LOT_NOT_FOUND",
                        "Lot not found for SKU: " + request.productSku()
                                + " and lot: " + request.lotNumber()
                ));

        StockBalanceEntity stock = stockBalanceRepository
                .findByProductIdAndWarehouseId(
                        product.getId(),
                        order.getWarehouse().getId()
                )
                .orElseThrow(() -> new BusinessException(
                        "STOCK_NOT_FOUND",
                        "Stock not found for SKU: " + request.productSku()
                ));

        BigDecimal before = stock.getQuantity();

        if (before.compareTo(request.quantity()) < 0) {
            throw new BusinessException(
                    "INSUFFICIENT_STOCK",
                    "Insufficient stock for SKU: " + request.productSku()
            );
        }

        BigDecimal after = before.subtract(request.quantity());

        stock.setQuantity(after);
        stock.setUpdatedAt(LocalDateTime.now());

        stockBalanceRepository.save(stock);

        InventoryMovementEntity movement = new InventoryMovementEntity();

        movement.setId(UUID.randomUUID());
        movement.setProduct(product);
        movement.setWarehouse(order.getWarehouse());
        movement.setMovementType(MovementType.PRODUCTION_CONSUMPTION);
        movement.setQuantity(request.quantity());
        movement.setBeforeQuantity(before);
        movement.setAfterQuantity(after);
        movement.setReferenceType(ReferenceType.MANUFACTURING_ORDER);
        movement.setReferenceId(order.getId());
        movement.setNotes(request.notes());
        movement.setCreatedAt(LocalDateTime.now());
        movement.setProductLot(lot);

        inventoryMovementRepository.save(movement);

        ManufacturingOrderInputEntity input = new ManufacturingOrderInputEntity();

        input.setId(UUID.randomUUID());
        input.setManufacturingOrder(order);
        input.setProduct(product);
        input.setPlannedQuantity(request.quantity());
        input.setActualQuantity(request.quantity());
        input.setInventoryMovement(movement);

        manufacturingOrderInputRepository.save(input);

        return new AddManufacturingInputResponse(
                input.getId(),
                order.getOrderNumber(),
                product.getSku(),
                request.quantity(),
                before,
                after
        );
    }

    @Transactional
    public AddManufacturingOutputResponse addOutput(
            String orderNumber,
            AddManufacturingOutputRequest request
    ) {
        ManufacturingOrderEntity order = manufacturingOrderRepository
                .findByOrderNumber(orderNumber)
                .orElseThrow(() -> new BusinessException(
                        "MANUFACTURING_ORDER_NOT_FOUND",
                        "Manufacturing order not found: " + orderNumber
                ));

        if (order.getStatus() != ManufacturingOrderStatus.IN_PROGRESS) {
            throw new BusinessException(
                    "INVALID_MANUFACTURING_ORDER_STATUS",
                    "Manufacturing order must be IN_PROGRESS"
            );
        }

        ProductEntity product = productRepository
                .findBySku(request.productSku())
                .orElseThrow(() -> new BusinessException(
                        "PRODUCT_NOT_FOUND",
                        "Product not found for SKU: " + request.productSku()
                ));

        StockBalanceEntity stock = stockBalanceRepository
                .findByProductIdAndWarehouseId(
                        product.getId(),
                        order.getWarehouse().getId()
                )
                .orElseGet(() -> {
                    StockBalanceEntity newStock = new StockBalanceEntity();
                    newStock.setId(UUID.randomUUID());
                    newStock.setProduct(product);
                    newStock.setWarehouse(order.getWarehouse());
                    newStock.setQuantity(BigDecimal.ZERO);
                    newStock.setCreatedAt(LocalDateTime.now());
                    return stockBalanceRepository.save(newStock);
                });

        BigDecimal before = stock.getQuantity();
        BigDecimal after = before.add(request.quantity());

        stock.setQuantity(after);
        stock.setUpdatedAt(LocalDateTime.now());
        stockBalanceRepository.save(stock);

        String generatedLotNumber = "BATCH-" + order.getOrderNumber();
        ProductLotEntity lot = new ProductLotEntity();
        lot.setId(UUID.randomUUID());
        lot.setProduct(product);
        lot.setLotNumber(generatedLotNumber);
        lot.setUnitPrice(null);
        lot.setPresentationQuantity(request.quantity());
        lot.setPresentationUnit("KG");
        lot.setReceivedAt(LocalDateTime.now());
        lot.setCreatedAt(LocalDateTime.now());
        productLotRepository.save(lot);

        InventoryMovementEntity movement = new InventoryMovementEntity();

        movement.setId(UUID.randomUUID());
        movement.setProduct(product);
        movement.setWarehouse(order.getWarehouse());
        movement.setMovementType(MovementType.PRODUCTION_OUTPUT);
        movement.setQuantity(request.quantity());
        movement.setBeforeQuantity(before);
        movement.setAfterQuantity(after);
        movement.setReferenceType(ReferenceType.MANUFACTURING_ORDER);
        movement.setReferenceId(order.getId());
        movement.setNotes(request.notes());
        movement.setCreatedAt(LocalDateTime.now());
        movement.setProductLot(lot);

        inventoryMovementRepository.save(movement);

        ManufacturingOrderOutputEntity output = new ManufacturingOrderOutputEntity();

        output.setId(UUID.randomUUID());
        output.setManufacturingOrder(order);
        output.setProduct(product);
        output.setQuantity(request.quantity());
        output.setInventoryMovement(movement);

        manufacturingOrderOutputRepository.save(output);

        return new AddManufacturingOutputResponse(
                output.getId(),
                order.getOrderNumber(),
                product.getSku(),
                generatedLotNumber,
                request.quantity(),
                before,
                after
        );
    }

    @Transactional
    public CompleteManufacturingOrderResponse complete(String orderNumber) {

        ManufacturingOrderEntity order = manufacturingOrderRepository
                .findByOrderNumber(orderNumber)
                .orElseThrow(() -> new BusinessException(
                        "MANUFACTURING_ORDER_NOT_FOUND",
                        "Manufacturing order not found: " + orderNumber
                ));

        if (order.getStatus() != ManufacturingOrderStatus.IN_PROGRESS) {
            throw new BusinessException(
                    "INVALID_MANUFACTURING_ORDER_STATUS",
                    "Only IN_PROGRESS manufacturing orders can be completed"
            );
        }

        if (!manufacturingOrderInputRepository.existsByManufacturingOrderId(order.getId())) {
            throw new BusinessException(
                    "MANUFACTURING_ORDER_WITHOUT_INPUTS",
                    "Manufacturing order has no consumed inputs"
            );
        }

        if (!manufacturingOrderOutputRepository.existsByManufacturingOrderId(order.getId())) {
            throw new BusinessException(
                    "MANUFACTURING_ORDER_WITHOUT_OUTPUTS",
                    "Manufacturing order has no produced outputs"
            );
        }

        BigDecimal actualQuantity = manufacturingOrderOutputRepository
                .sumQuantityByManufacturingOrderId(order.getId());

        order.setActualQuantity(actualQuantity);
        order.setStatus(ManufacturingOrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());

        manufacturingOrderRepository.save(order);

        return new CompleteManufacturingOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getActualQuantity(),
                order.getStatus(),
                order.getCompletedAt()
        );
    }

    @Transactional(readOnly = true)
    public Page<ManufacturingOrderResponse> findAll(
            ManufacturingOrderStatus status,
            String orderNumber,
            String productSku,
            String formulaCode,
            Pageable pageable
    ) {
        Specification<ManufacturingOrderEntity> spec = Specification.unrestricted();

        if (status != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status)
            );
        }

        if (orderNumber != null && !orderNumber.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(
                            cb.lower(root.get("orderNumber")),
                            "%" + orderNumber.toLowerCase() + "%"
                    )
            );
        }

        if (productSku != null && !productSku.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(
                            cb.lower(root.get("product").get("sku")),
                            "%" + productSku.toLowerCase() + "%"
                    )
            );
        }

        if (formulaCode != null && !formulaCode.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(
                            cb.lower(root.get("formula").get("code")),
                            "%" + formulaCode.toLowerCase() + "%"
                    )
            );
        }

        return manufacturingOrderRepository.findAll(spec, pageable)
                .map(this::toResponse);
    }

    private ManufacturingOrderResponse toResponse(ManufacturingOrderEntity order) {
        return new ManufacturingOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getFormula().getCode(),
                order.getFormula().getVersion(),
                order.getProduct().getSku(),
                order.getPlannedQuantity(),
                order.getActualQuantity(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getStartedAt(),
                order.getCompletedAt()
        );
    }

}