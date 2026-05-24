package mx.terabyte.labs.inventra.catalog.category;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mx.terabyte.labs.inventra.catalog.category.dto.CreateProductCategoryRequest;
import mx.terabyte.labs.inventra.catalog.category.dto.ProductCategoryResponse;
import mx.terabyte.labs.inventra.catalog.category.dto.UpdateProductCategoryRequest;
import mx.terabyte.labs.inventra.common.api.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/product-categories")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService service;

    @GetMapping
    public ApiResponse<Page<ProductCategoryResponse>> findAll(
            @RequestParam(name = "name", required = false)
            String name,

            @PageableDefault(
                    size = 20,
                    sort = "name",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        return ApiResponse.ok(
                service.findAll(name, pageable)
        );
    }

    @PostMapping
    public ApiResponse<ProductCategoryResponse> create(
            @Valid @RequestBody CreateProductCategoryRequest request
    ) {
        return ApiResponse.ok(
                service.create(request)
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductCategoryResponse> update(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateProductCategoryRequest request
    ) {
        return ApiResponse.ok(
                service.update(id, request)
        );
    }
}