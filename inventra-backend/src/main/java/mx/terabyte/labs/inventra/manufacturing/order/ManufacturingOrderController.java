package mx.terabyte.labs.inventra.manufacturing.order;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mx.terabyte.labs.inventra.common.api.ApiResponse;
import mx.terabyte.labs.inventra.common.enums.ManufacturingOrderStatus;
import mx.terabyte.labs.inventra.inventory.movement.dto.InventoryMovementResponse;
import mx.terabyte.labs.inventra.manufacturing.order.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/{orderNumber}")
    public ApiResponse<ManufacturingOrderResponse> findByOrderNumber(
            @PathVariable("orderNumber") String orderNumber
    ) {
        return ApiResponse.ok(
                manufacturingOrderService.findByOrderNumber(orderNumber)
        );
    }

    @GetMapping("/{orderNumber}/inputs")
    public ApiResponse<List<ManufacturingOrderInputResponse>> findInputs(
            @PathVariable("orderNumber") String orderNumber
    ) {
        return ApiResponse.ok(
                manufacturingOrderService.findInputsByOrderNumber(orderNumber)
        );
    }

    @GetMapping("/{orderNumber}/outputs")
    public ApiResponse<List<ManufacturingOrderOutputResponse>> findOutputs(
            @PathVariable("orderNumber") String orderNumber
    ) {
        return ApiResponse.ok(
                manufacturingOrderService.findOutputsByOrderNumber(orderNumber)
        );
    }

    @GetMapping("/{orderNumber}/movements")
    public ApiResponse<List<InventoryMovementResponse>> findMovements(
            @PathVariable("orderNumber") String orderNumber
    ) {
        return ApiResponse.ok(
                manufacturingOrderService.findMovementsByOrderNumber(orderNumber)
        );
    }

    @GetMapping("/{orderNumber}/steps")
    public ApiResponse<List<ManufacturingOrderStepResponse>> findSteps(
            @PathVariable("orderNumber") String orderNumber
    ) {
        return ApiResponse.ok(
                manufacturingOrderService.findStepsByOrderNumber(orderNumber)
        );
    }

}