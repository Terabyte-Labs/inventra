package mx.terabyte.labs.inventra.manufacturing.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.UUID;

public interface ManufacturingOrderOutputRepository
        extends JpaRepository<ManufacturingOrderOutputEntity, UUID> {

    boolean existsByManufacturingOrderId(UUID manufacturingOrderId);

    @Query("""
        SELECT COALESCE(SUM(o.quantity), 0)
        FROM ManufacturingOrderOutputEntity o
        WHERE o.manufacturingOrder.id = :manufacturingOrderId
    """)
    BigDecimal sumQuantityByManufacturingOrderId(
        @Param("manufacturingOrderId") UUID manufacturingOrderId
    );

}