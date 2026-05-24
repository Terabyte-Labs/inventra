package mx.terabyte.labs.inventra.inventory.stock;

import mx.terabyte.labs.inventra.inventory.stock.dto.StockBalanceResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockBalanceRepository extends JpaRepository<StockBalanceEntity, UUID> {

    Optional<StockBalanceEntity> findByProductIdAndWarehouseId(
            UUID productId,
            UUID warehouseId
    );

    @Query("""
                SELECT new mx.terabyte.labs.inventra.inventory.stock.dto.StockBalanceResponse(
                    s.id,
                    p.sku,
                    p.name,
                    CAST(p.productType AS string),
                    w.code,
                    w.name,
                    s.quantity,
                    p.minStock,
                    s.updatedAt
                )
                FROM StockBalanceEntity s
                JOIN s.product p
                JOIN s.warehouse w
                ORDER BY p.name ASC
            """)
    List<StockBalanceResponse> findAllProjected();

}