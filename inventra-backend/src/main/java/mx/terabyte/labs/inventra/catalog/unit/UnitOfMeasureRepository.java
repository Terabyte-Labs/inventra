package mx.terabyte.labs.inventra.catalog.unit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasureEntity, UUID> {

    Optional<UnitOfMeasureEntity> findByCode(String code);
}