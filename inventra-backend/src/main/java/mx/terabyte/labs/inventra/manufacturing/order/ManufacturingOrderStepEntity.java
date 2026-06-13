package mx.terabyte.labs.inventra.manufacturing.order;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mx.terabyte.labs.inventra.common.enums.ManufacturingOrderStepStatus;
import mx.terabyte.labs.inventra.common.enums.ManufacturingProcessStepType;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "manufacturing_order_steps",
        schema = "manufacturing",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_manufacturing_order_step_number",
                        columnNames = {"manufacturing_order_id", "step_number"}
                )
        }
)
public class ManufacturingOrderStepEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manufacturing_order_id", nullable = false)
    private ManufacturingOrderEntity manufacturingOrder;

    @Column(name = "step_number", nullable = false)
    private Integer stepNumber;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "step_type", nullable = false, length = 60)
    private ManufacturingProcessStepType stepType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ManufacturingOrderStepStatus status;

    @Column(name = "requires_quality_check", nullable = false)
    private Boolean requiresQualityCheck;

    @Column(name = "expected_duration_minutes")
    private Integer expectedDurationMinutes;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "quality_result", length = 40)
    private String qualityResult;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
