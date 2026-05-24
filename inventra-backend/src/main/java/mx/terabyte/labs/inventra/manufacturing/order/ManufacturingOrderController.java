package mx.terabyte.labs.inventra.manufacturing.order;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mx.terabyte.labs.inventra.common.api.ApiResponse;
import mx.terabyte.labs.inventra.common.enums.ManufacturingOrderStatus;
import mx.terabyte.labs.inventra.manufacturing.order.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/manufacturing/orders")
@RequiredArgsConstructor
public class ManufacturingOrderController {

    private final ManufacturingOrderService manufacturingOrderService;

    @PostMapping
    public ApiResponse<CreateManufacturingOrderResponse> create(
            @Valid @RequestBody CreateManufacturingOrderRequest request
    ) {
        return ApiResponse.ok(
                manufacturingOrderService.create(request)
        );
    }

    @PostMapping("/{orderNumber}/start")
    public ApiResponse<StartManufacturingOrderResponse> start(
            @PathVariable("orderNumber") String orderNumber
    ) {
        return ApiResponse.ok(
                manufacturingOrderService.start(orderNumber)
        );
    }

    @PostMapping("/{orderNumber}/inputs")
    public ApiResponse<AddManufacturingInputResponse> addInput(
            @PathVariable("orderNumber") String orderNumber,
            @Valid @RequestBody AddManufacturingInputRequest request
    ) {
        return ApiResponse.ok(
                manufacturingOrderService.addInput(orderNumber, request)
        );
    }

    @PostMapping("/{orderNumber}/outputs")
    public ApiResponse<AddManufacturingOutputResponse> addOutput(
            @PathVariable("orderNumber") String orderNumber,
            @Valid @RequestBody AddManufacturingOutputRequest request
    ) {
        return ApiResponse.ok(
                manufacturingOrderService.addOutput(orderNumber, request)
        );
    }

    @PostMapping("/{orderNumber}/complete")
    public ApiResponse<CompleteManufacturingOrderResponse> complete(
            @PathVariable("orderNumber") String orderNumber
    ) {
        return ApiResponse.ok(
                manufacturingOrderService.complete(orderNumber)
        );
    }

    @GetMapping
    public ApiResponse<Page<ManufacturingOrderResponse>> findAll(
            @RequestParam(name = "status", required = false)
            ManufacturingOrderStatus status,

            @RequestParam(name = "orderNumber", required = false)
            String orderNumber,

            @RequestParam(name = "productSku", required = false)
            String productSku,

            @RequestParam(name = "formulaCode", required = false)
            String formulaCode,

            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ApiResponse.ok(
                manufacturingOrderService.findAll(
                        status,
                        orderNumber,
                        productSku,
                        formulaCode,
                        pageable
                )
        );
    }

}