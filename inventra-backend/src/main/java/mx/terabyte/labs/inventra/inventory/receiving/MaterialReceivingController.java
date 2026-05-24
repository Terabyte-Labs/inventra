package mx.terabyte.labs.inventra.inventory.receiving;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mx.terabyte.labs.inventra.common.api.ApiResponse;
import mx.terabyte.labs.inventra.inventory.receiving.dto.ReceiveMaterialRequest;
import mx.terabyte.labs.inventra.inventory.receiving.dto.ReceiveMaterialResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory/receivings")
@RequiredArgsConstructor
public class MaterialReceivingController {

    private final MaterialReceivingService materialReceivingService;

    @PostMapping
    public ApiResponse<ReceiveMaterialResponse> receive(
            @Valid @RequestBody ReceiveMaterialRequest request
    ) {
        return ApiResponse.ok(
                materialReceivingService.receive(request)
        );
    }
}