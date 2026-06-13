package mx.terabyte.labs.inventra.manufacturing.formula;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FormulaProcessStepRepository
        extends JpaRepository<FormulaProcessStepEntity, UUID> {

    List<FormulaProcessStepEntity> findByFormulaIdOrderByStepNumberAsc(UUID formulaId);

}
