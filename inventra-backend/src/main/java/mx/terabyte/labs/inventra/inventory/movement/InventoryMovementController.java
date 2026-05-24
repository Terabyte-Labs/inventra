package mx.terabyte.labs.inventra.inventory.movement;

import lombok.RequiredArgsConstructor;
import mx.terabyte.labs.inventra.common.api.ApiResponse;
import mx.terabyte.labs.inventra.inventory.movement.dto.InventoryMovementResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/movements")
@RequiredArgsConstructor
public class InventoryMovementController {

    private final InventoryMovementService service;

    @GetMapping
    public ApiResponse<List<InventoryMovementResponse>> findAll() {
        return ApiResponse.ok(service.findAll());
    }
}