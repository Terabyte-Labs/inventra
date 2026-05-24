package mx.terabyte.labs.inventra.catalog.unit;

import lombok.RequiredArgsConstructor;
import mx.terabyte.labs.inventra.catalog.unit.dto.UnitOfMeasureResponse;
import mx.terabyte.labs.inventra.common.api.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/units-of-measure")
@RequiredArgsConstructor
public class UnitOfMeasureController {

    private final UnitOfMeasureService service;

    @GetMapping
    public ApiResponse<List<UnitOfMeasureResponse>> findAll() {
        return ApiResponse.ok(service.findAll());
    }
}