package mx.terabyte.labs.inventra.inventory.stock;

import lombok.RequiredArgsConstructor;
import mx.terabyte.labs.inventra.common.api.ApiResponse;
import mx.terabyte.labs.inventra.inventory.stock.dto.StockBalanceResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/stock")
@RequiredArgsConstructor
public class StockBalanceController {

    private final StockBalanceService service;

    @GetMapping
    public ApiResponse<List<StockBalanceResponse>> findAll() {
        return ApiResponse.ok(service.findAll());
    }
}