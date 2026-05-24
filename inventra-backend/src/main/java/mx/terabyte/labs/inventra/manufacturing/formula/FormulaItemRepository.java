package mx.terabyte.labs.inventra.manufacturing.formula;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FormulaItemRepository extends JpaRepository<FormulaItemEntity, UUID> {

    List<FormulaItemEntity> findByFormulaIdOrderByLineOrderAsc(UUID formulaId);

}