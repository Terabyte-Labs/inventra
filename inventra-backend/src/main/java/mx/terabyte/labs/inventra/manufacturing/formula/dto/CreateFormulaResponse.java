package mx.terabyte.labs.inventra.manufacturing.formula.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateFormulaResponse(

        UUID formulaId,

        String code,

        String name,

        String outputProductSku,

        BigDecimal outputQuantity,

        Integer itemsCount,

        Integer version

) {
}