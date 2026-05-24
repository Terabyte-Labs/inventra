package mx.terabyte.labs.inventra.manufacturing.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ManufacturingOrderRepository
        extends JpaRepository<ManufacturingOrderEntity, UUID>,
        JpaSpecificationExecutor<ManufacturingOrderEntity> {

    Optional<ManufacturingOrderEntity> findByOrderNumber(String orderNumber);
}