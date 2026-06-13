package mx.terabyte.labs.inventra.manufacturing.formula;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.terabyte.labs.inventra.catalog.product.ProductEntity;
import mx.terabyte.labs.inventra.catalog.product.ProductRepository;
import mx.terabyte.labs.inventra.catalog.unit.UnitOfMeasureEntity;
import mx.terabyte.labs.inventra.catalog.unit.UnitOfMeasureRepository;
import mx.terabyte.labs.inventra.common.exception.BusinessException;
import mx.terabyte.labs.inventra.manufacturing.formula.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FormulaService {

    private final FormulaRepository formulaRepository;
    private final FormulaItemRepository formulaItemRepository;
    private final FormulaProcessStepRepository formulaProcessStepRepository;
    private final ProductRepository productRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;

    @Transactional
    public FormulaDetailResponse create(
            CreateFormulaRequest request
    ) {
        log.info("Creating new formula: code={}, version={}, outputProductSku={}",
                request.code(), request.version(), request.outputProductSku());

        formulaRepository.findByCodeAndVersion(request.code(), request.version())
                .ifPresent(existing -> {
                    log.warn("Formula already exists: code={}, version={}", request.code(), request.version());
                    throw new BusinessException(
                            "FORMULA_ALREADY_EXISTS",
                            "Formula already exists with code: " + request.code()
                                    + " and version: " + request.version()
                    );
                });

        ProductEntity outputProduct = productRepository
                .findBySku(request.outputProductSku())
                .orElseThrow(() -> {
                    log.error("Output product not found for formula: sku={}", request.outputProductSku());
                    return new BusinessException(
                            "PRODUCT_NOT_FOUND",
                            "Output product not found: " + request.outputProductSku()
                    );
                });

        UnitOfMeasureEntity outputUnit = unitOfMeasureRepository
                .findByCode(request.outputUnitOfMeasureCode())
                .orElseThrow(() -> new BusinessException(
                        "UNIT_OF_MEASURE_NOT_FOUND",
                        "Unit of measure not found for code: " + request.outputUnitOfMeasureCode()
                ));

        validateUniqueProcessStepNumbers(request.processSteps());

        FormulaEntity formula = new FormulaEntity();

        formula.setId(UUID.randomUUID());
        formula.setCode(request.code());
        formula.setVersion(request.version());
        formula.setName(request.name());
        formula.setProduct(outputProduct);
        formula.setOutputQuantity(request.outputQuantity());
        formula.setActive(true);
        formula.setCreatedAt(LocalDateTime.now());
        formula.setOutputUnitOfMeasure(outputUnit);

        formulaRepository.save(formula);

        Map<Integer, FormulaProcessStepEntity> processStepsByNumber =
                createProcessSteps(formula, request.processSteps());

        int line = 1;

        for (CreateFormulaItemRequest itemRequest : request.items()) {

            ProductEntity rawMaterial = productRepository
                    .findBySku(itemRequest.productSku())
                    .orElseThrow(() -> new BusinessException(
                            "PRODUCT_NOT_FOUND",
                            "Raw material not found: " + itemRequest.productSku()
                    ));

            UnitOfMeasureEntity itemUnit = unitOfMeasureRepository
                    .findByCode(itemRequest.unitOfMeasureCode())
                    .orElseThrow(() -> new BusinessException(
                            "UNIT_OF_MEASURE_NOT_FOUND",
                            "Unit of measure not found for code: " + itemRequest.unitOfMeasureCode()
                    ));

            FormulaProcessStepEntity processStep = null;

            if (itemRequest.processStepNumber() != null) {
                processStep = processStepsByNumber.get(itemRequest.processStepNumber());

                if (processStep == null) {
                    throw new BusinessException(
                            "FORMULA_PROCESS_STEP_NOT_FOUND",
                            "Process step not found for step number: "
                                    + itemRequest.processStepNumber()
                    );
                }
            }

            FormulaItemEntity item = new FormulaItemEntity();

            item.setId(UUID.randomUUID());
            item.setFormula(formula);
            item.setProduct(rawMaterial);
            item.setProcessStep(processStep);
            item.setQuantity(itemRequest.quantity());
            item.setUnitOfMeasure(itemUnit);
            item.setLineOrder(line++);
            item.setNotes(itemRequest.notes());

            formulaItemRepository.save(item);
        }

        return findByCodeAndVersion(formula.getCode(), formula.getVersion());
    }

    private void validateUniqueProcessStepNumbers(
            List<CreateFormulaProcessStepRequest> processSteps
    ) {
        long distinctCount = processSteps.stream()
                .map(CreateFormulaProcessStepRequest::stepNumber)
                .distinct()
                .count();

        if (distinctCount != processSteps.size()) {
            throw new BusinessException(
                    "DUPLICATED_FORMULA_PROCESS_STEP",
                    "Formula process step numbers must be unique"
            );
        }
    }

    private Map<Integer, FormulaProcessStepEntity> createProcessSteps(
            FormulaEntity formula,
            List<CreateFormulaProcessStepRequest> processStepRequests
    ) {
        return processStepRequests
                .stream()
                .sorted(Comparator.comparing(CreateFormulaProcessStepRequest::stepNumber))
                .map(request -> {
                    FormulaProcessStepEntity step = new FormulaProcessStepEntity();

                    step.setId(UUID.randomUUID());
                    step.setFormula(formula);
                    step.setStepNumber(request.stepNumber());
                    step.setName(request.name());
                    step.setDescription(request.description());
                    step.setStepType(request.stepType());
                    step.setRequiresQualityCheck(request.requiresQualityCheck());
                    step.setExpectedDurationMinutes(request.expectedDurationMinutes());
                    step.setActive(true);

                    return formulaProcessStepRepository.save(step);
                })
                .collect(Collectors.toMap(
                        FormulaProcessStepEntity::getStepNumber,
                        Function.identity()
                ));
    }

    @Transactional(readOnly = true)
    public Page<FormulaSearchResponse> findAll(
            String code,
            String name,
            String outputProductSku,
            Pageable pageable
    ) {
        Specification<FormulaEntity> spec = Specification.unrestricted();

        if (code != null && !code.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(
                            cb.lower(root.get("code")),
                            "%" + code.toLowerCase() + "%"
                    )
            );
        }

        if (name != null && !name.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(
                            cb.lower(root.get("name")),
                            "%" + name.toLowerCase() + "%"
                    )
            );
        }

        if (outputProductSku != null && !outputProductSku.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(
                            cb.lower(root.get("product").get("sku")),
                            "%" + outputProductSku.toLowerCase() + "%"
                    )
            );
        }

        return formulaRepository.findAll(spec, pageable)
                .map(this::toSearchResponse);
    }

    @Transactional(readOnly = true)
    public FormulaDetailResponse findByCodeAndVersion(
            String code,
            Integer version
    ) {
        FormulaEntity formula = formulaRepository
                .findByCodeAndVersion(code, version)
                .orElseThrow(() -> new BusinessException(
                        "FORMULA_NOT_FOUND",
                        "Formula not found for code: " + code
                                + " and version: " + version
                ));

        List<FormulaItemResponse> items = formulaItemRepository
                .findByFormulaId(formula.getId())
                .stream()
                .map(this::toItemResponse)
                .toList();

        List<FormulaProcessStepResponse> processSteps = formulaProcessStepRepository
                .findByFormulaIdOrderByStepNumberAsc(formula.getId())
                .stream()
                .map(this::toProcessStepResponse)
                .toList();

        ProductEntity outputProduct = formula.getProduct();

        return new FormulaDetailResponse(
                formula.getId(),
                formula.getCode(),
                formula.getVersion(),
                formula.getName(),
                formula.getActive(),

                outputProduct.getSku(),
                outputProduct.getName(),
                outputProduct.getProductType().name(),
                outputProduct.getCategory() != null
                        ? outputProduct.getCategory().getName()
                        : null,

                formula.getOutputQuantity(),
                formula.getOutputUnitOfMeasure().getCode(),
                formula.getOutputUnitOfMeasure().getName(),

                outputProduct.getUnitOfMeasure().getCode(),
                outputProduct.getUnitOfMeasure().getName(),

                items.size(),
                items,

                processSteps.size(),
                processSteps,

                formula.getCreatedAt()
        );
    }

    private FormulaSearchResponse toSearchResponse(FormulaEntity formula) {
        Integer itemsCount = formulaItemRepository
                .findByFormulaId(formula.getId())
                .size();

        Integer processStepsCount = formulaProcessStepRepository
                .findByFormulaIdOrderByStepNumberAsc(formula.getId())
                .size();

        ProductEntity outputProduct = formula.getProduct();

        return new FormulaSearchResponse(
                formula.getId(),
                formula.getCode(),
                formula.getVersion(),
                formula.getName(),
                formula.getActive(),

                outputProduct.getSku(),
                outputProduct.getName(),
                outputProduct.getProductType().name(),
                outputProduct.getCategory() != null
                        ? outputProduct.getCategory().getName()
                        : null,

                formula.getOutputQuantity(),
                formula.getOutputUnitOfMeasure().getCode(),
                formula.getOutputUnitOfMeasure().getName(),

                itemsCount,
                processStepsCount
        );
    }


    private FormulaItemResponse toItemResponse(FormulaItemEntity item) {
        ProductEntity product = item.getProduct();
        FormulaProcessStepEntity processStep = item.getProcessStep();

        return new FormulaItemResponse(
                item.getId(),
                product.getSku(),
                product.getName(),
                product.getProductType().name(),
                product.getCategory() != null ? product.getCategory().getName() : null,

                item.getQuantity(),
                item.getUnitOfMeasure().getCode(),
                item.getUnitOfMeasure().getName(),

                product.getUnitOfMeasure().getCode(),
                product.getUnitOfMeasure().getName(),

                processStep != null ? processStep.getStepNumber() : null,
                processStep != null ? processStep.getName() : null,
                processStep != null ? processStep.getStepType().name() : null
        );
    }

    private FormulaProcessStepResponse toProcessStepResponse(
            FormulaProcessStepEntity step
    ) {
        return new FormulaProcessStepResponse(
                step.getId(),
                step.getStepNumber(),
                step.getName(),
                step.getDescription(),
                step.getStepType(),
                step.getRequiresQualityCheck(),
                step.getExpectedDurationMinutes(),
                step.getActive()
        );
    }
}
