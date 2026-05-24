package mx.terabyte.labs.inventra.manufacturing.formula;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.terabyte.labs.inventra.catalog.product.ProductEntity;
import mx.terabyte.labs.inventra.catalog.product.ProductRepository;
import mx.terabyte.labs.inventra.common.exception.BusinessException;
import mx.terabyte.labs.inventra.manufacturing.formula.dto.CreateFormulaItemRequest;
import mx.terabyte.labs.inventra.manufacturing.formula.dto.CreateFormulaRequest;
import mx.terabyte.labs.inventra.manufacturing.formula.dto.CreateFormulaResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FormulaService {

    private final FormulaRepository formulaRepository;
    private final FormulaItemRepository formulaItemRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CreateFormulaResponse create(
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

        FormulaEntity formula = new FormulaEntity();

        formula.setId(UUID.randomUUID());
        formula.setCode(request.code());
        formula.setName(request.name());
        formula.setProduct(outputProduct);
        formula.setOutputQuantity(request.outputQuantity());
        formula.setVersion(request.version());
        formula.setActive(true);
        formula.setCreatedAt(LocalDateTime.now());

        formulaRepository.save(formula);

        int line = 1;

        for (CreateFormulaItemRequest itemRequest : request.items()) {

            ProductEntity rawMaterial = productRepository
                    .findBySku(itemRequest.productSku())
                    .orElseThrow(() -> new BusinessException(
                            "PRODUCT_NOT_FOUND",
                            "Raw material not found: " + itemRequest.productSku()
                    ));

            FormulaItemEntity item = new FormulaItemEntity();

            item.setId(UUID.randomUUID());
            item.setFormula(formula);
            item.setProduct(rawMaterial);
            item.setQuantity(itemRequest.quantity());
            item.setLineOrder(line++);
            item.setNotes(itemRequest.notes());

            formulaItemRepository.save(item);
        }

        return new CreateFormulaResponse(
                formula.getId(),
                formula.getCode(),
                formula.getName(),
                outputProduct.getSku(),
                formula.getOutputQuantity(),
                request.items().size(),
                formula.getVersion()
        );
    }
}