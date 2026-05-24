package mx.terabyte.labs.inventra.catalog.supplier;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupplierContactRepository
        extends JpaRepository<SupplierContactEntity, UUID> {
    List<SupplierContactEntity> findBySupplierId(UUID supplierId);
    boolean existsBySupplierIdAndEmail(
            UUID supplierId,
            String email
    );
    Optional<SupplierContactEntity> findByIdAndSupplierId(
            UUID id,
            UUID supplierId
    );

}
