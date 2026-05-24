package mx.terabyte.labs.inventra.catalog.supplier;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface SupplierRepository
        extends JpaRepository<SupplierEntity, UUID>,
        JpaSpecificationExecutor<SupplierEntity> {
    Optional<SupplierEntity> findByName(String name);
    Optional<SupplierEntity> findByCode(String code);
}