package mx.terabyte.labs.inventra.manufacturing.formula;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mx.terabyte.labs.inventra.common.enums.ManufacturingProcessStepType;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "formula_process_steps",
        schema = "manufacturing",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_formula_process_step_number",
                        columnNames = {"formula_id", "step_number"}
                )
        }
)
public class FormulaProcessStepEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formula_id", nullable = false)
    private FormulaEntity formula;

    @Column(name = "step_number", nullable = false)
    private Integer stepNumber;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "step_type", nullable = false, length = 60)
    private ManufacturingProcessStepType stepType;

    @Column(name = "requires_quality_check", nullable = false)
    private Boolean requiresQualityCheck;

    @Column(name = "expected_duration_minutes")
    private Integer expectedDurationMinutes;

    @Column(nullable = false)
    private Boolean active;
}
