package mx.terabyte.labs.inventra.manufacturing.formula;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface FormulaRepository
        extends JpaRepository<FormulaEntity, UUID>,
        JpaSpecificationExecutor<FormulaEntity> {

    Optional<FormulaEntity> findByCodeAndVersion(
            String code,
            Integer version
    );
}