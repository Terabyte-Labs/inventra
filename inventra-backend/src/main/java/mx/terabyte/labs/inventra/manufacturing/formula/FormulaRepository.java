package mx.terabyte.labs.inventra.manufacturing.formula;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FormulaRepository extends JpaRepository<FormulaEntity, UUID> {

    Optional<FormulaEntity> findByCode(String code);
    Optional<FormulaEntity> findByCodeAndVersion(String code, Integer version);

}