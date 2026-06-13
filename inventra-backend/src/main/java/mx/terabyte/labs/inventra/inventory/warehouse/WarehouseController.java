package mx.terabyte.labs.inventra.inventory.warehouse;

import lombok.RequiredArgsConstructor;
import mx.terabyte.labs.inventra.common.api.ApiResponse;
import mx.terabyte.labs.inventra.inventory.warehouse.dto.WarehouseResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inventory/warehouses")
public class WarehouseController {

    private final WarehouseService service;

    @GetMapping
    public ApiResponse<List<WarehouseResponse>> findAllActive() {
        return ApiResponse.ok(service.findAllActive());
    }
}
