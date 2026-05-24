package mx.terabyte.labs.inventra.inventory.stock.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record StockBalanceResponse(
        UUID id,
        String productSku,
        String productName,
        String productType,
        String warehouseCode,
        String warehouseName,
        BigDecimal quantity,
        BigDecimal minStock,
        LocalDateTime updatedAt
) {
}