package mx.terabyte.labs.inventra.catalog.unit;

import mx.terabyte.labs.inventra.common.enums.UnitGroup;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UnitConversionServiceTest {

    @Test
    void shouldConvertGramsToKilograms() {
        UnitOfMeasureEntity grams = new UnitOfMeasureEntity();
        grams.setId(UUID.randomUUID());
        grams.setCode("G");
        grams.setName("Gramo");
        grams.setUnitGroup(UnitGroup.WEIGHT);
        grams.setConversionFactorToBase(new BigDecimal("1.000000"));
        grams.setCreatedAt(LocalDateTime.now());

        UnitOfMeasureEntity kilograms = new UnitOfMeasureEntity();
        kilograms.setId(UUID.randomUUID());
        kilograms.setCode("KG");
        kilograms.setName("Kilogramo");
        kilograms.setUnitGroup(UnitGroup.WEIGHT);
        kilograms.setConversionFactorToBase(new BigDecimal("1000.000000"));
        kilograms.setCreatedAt(LocalDateTime.now());

        UnitConversionService service = new UnitConversionService(null);

        BigDecimal result = service.convert(
                new BigDecimal("460"),
                grams,
                kilograms
        );
        assertEquals(0, result.compareTo(new BigDecimal("0.460000")));
    }

    @Test
    void shouldConvertLitersToMilliliters() {
        UnitOfMeasureEntity liters = new UnitOfMeasureEntity();
        liters.setId(UUID.randomUUID());
        liters.setCode("L");
        liters.setName("Litro");
        liters.setUnitGroup(UnitGroup.VOLUME);
        liters.setConversionFactorToBase(new BigDecimal("1000.000000"));
        liters.setCreatedAt(LocalDateTime.now());

        UnitOfMeasureEntity milliliters = new UnitOfMeasureEntity();
        milliliters.setId(UUID.randomUUID());
        milliliters.setCode("ML");
        milliliters.setName("Mililitro");
        milliliters.setUnitGroup(UnitGroup.VOLUME);
        milliliters.setConversionFactorToBase(new BigDecimal("1.000000"));
        milliliters.setCreatedAt(LocalDateTime.now());

        UnitConversionService service = new UnitConversionService(null);

        BigDecimal result = service.convert(
                new BigDecimal("1.5"),
                liters,
                milliliters
        );
        assertEquals(0, result.compareTo(new BigDecimal("1500.000000")));
    }
}