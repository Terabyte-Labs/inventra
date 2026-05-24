package mx.terabyte.labs.inventra.catalog.product;

import jakarta.validation.Valid;
import mx.terabyte.labs.inventra.catalog.product.dto.CreateProductRequest;
import mx.terabyte.labs.inventra.catalog.product.dto.ProductSearchResponse;
import mx.terabyte.labs.inventra.catalog.product.dto.UpdateProductRequest;
import mx.terabyte.labs.inventra.common.api.ApiResponse;
import mx.terabyte.labs.inventra.common.enums.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ApiResponse<Page<ProductSearchResponse>> findAll(
            @RequestParam(name = "sku", required = false) String sku,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "productType", required = false) ProductType productType,
            @RequestParam(name = "active", required = false) Boolean active,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return ApiResponse.ok(
                productService.findAll(sku, name, productType, active, pageable)
        );
    }

    @GetMapping("/{sku}")
    public ApiResponse<ProductSearchResponse> findBySku(
            @PathVariable("sku") String sku
    ) {
        return ApiResponse.ok(
                productService.findBySku(sku)
        );
    }

    @PutMapping("/{sku}")
    public ApiResponse<ProductSearchResponse> update(
            @PathVariable("sku") String sku,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        return ApiResponse.ok(
                productService.update(sku, request)
        );
    }

    @PostMapping
    public ApiResponse<ProductSearchResponse> create(
            @Valid @RequestBody CreateProductRequest request
    ) {
        return ApiResponse.ok(
                productService.create(request)
        );
    }
}