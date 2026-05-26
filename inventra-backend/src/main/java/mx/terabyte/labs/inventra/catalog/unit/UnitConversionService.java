package mx.terabyte.labs.inventra.catalog.unit;

import lombok.RequiredArgsConstructor;
import mx.terabyte.labs.inventra.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class UnitConversionService {

    private static final int SCALE = 6;

    private final UnitOfMeasureRepository unitOfMeasureRepository;

    public BigDecimal convert(
            BigDecimal quantity,
            String fromUnitCode,
            String toUnitCode
    ) {
        UnitOfMeasureEntity fromUnit = unitOfMeasureRepository
                .findByCode(fromUnitCode)
                .orElseThrow(() -> new BusinessException(
                        "UNIT_OF_MEASURE_NOT_FOUND",
                        "Unit of measure not found for code: " + fromUnitCode
                ));

        UnitOfMeasureEntity toUnit = unitOfMeasureRepository
                .findByCode(toUnitCode)
                .orElseThrow(() -> new BusinessException(
                        "UNIT_OF_MEASURE_NOT_FOUND",
                        "Unit of measure not found for code: " + toUnitCode
                ));

        return convert(quantity, fromUnit, toUnit);
    }

    public BigDecimal convert(
            BigDecimal quantity,
            UnitOfMeasureEntity fromUnit,
            UnitOfMeasureEntity toUnit
    ) {
        if (!fromUnit.getUnitGroup().equals(toUnit.getUnitGroup())) {
            throw new BusinessException(
                    "UNIT_CONVERSION_NOT_SUPPORTED",
                    "Cannot convert from "
                            + fromUnit.getCode()
                            + " to "
                            + toUnit.getCode()
            );
        }

        return quantity
                .multiply(fromUnit.getConversionFactorToBase())
                .divide(
                        toUnit.getConversionFactorToBase(),
                        SCALE,
                        RoundingMode.HALF_UP
                );
    }
}