package mx.terabyte.labs.inventra.catalog.producttype;

import mx.terabyte.labs.inventra.catalog.product.dto.ProductTypeResponse;
import mx.terabyte.labs.inventra.common.api.ApiResponse;
import mx.terabyte.labs.inventra.common.enums.ProductType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/product-types")
public class ProductTypeController {

    @GetMapping
    public ApiResponse<List<ProductTypeResponse>> findAll() {
        return ApiResponse.ok(
                Arrays.stream(ProductType.values())
                        .map(type -> new ProductTypeResponse(
                                type.getCode(),
                                type.getDisplayName()
                        ))
                        .toList()
        );
    }
}
