package mx.terabyte.labs.inventra.catalog.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ProductCategoryRepository
        extends JpaRepository<ProductCategoryEntity, UUID>,
        JpaSpecificationExecutor<ProductCategoryEntity> {

    Optional<ProductCategoryEntity> findByName(String name);
}