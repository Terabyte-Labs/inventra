package mx.terabyte.labs.inventra.inventory.dispatch;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mx.terabyte.labs.inventra.common.api.ApiResponse;
import mx.terabyte.labs.inventra.inventory.dispatch.dto.DispatchInventoryRequest;
import mx.terabyte.labs.inventra.inventory.dispatch.dto.DispatchInventoryResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory/dispatches")
@RequiredArgsConstructor
public class InventoryDispatchController {

    private final InventoryDispatchService service;

    @PostMapping
    public ApiResponse<DispatchInventoryResponse> dispatch(
            @Valid @RequestBody DispatchInventoryRequest request
    ) {
        return ApiResponse.ok(
                service.dispatch(request)
        );
    }
}