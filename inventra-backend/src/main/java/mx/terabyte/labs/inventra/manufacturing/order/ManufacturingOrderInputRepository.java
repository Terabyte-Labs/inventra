package mx.terabyte.labs.inventra.manufacturing.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ManufacturingOrderInputRepository
        extends JpaRepository<ManufacturingOrderInputEntity, UUID> {
    boolean existsByManufacturingOrderId(UUID manufacturingOrderId);
}