package mx.terabyte.labs.inventra.catalog.supplier;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mx.terabyte.labs.inventra.catalog.supplier.dto.*;
import mx.terabyte.labs.inventra.common.api.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService service;

    @GetMapping
    public ApiResponse<Page<SupplierResponse>> findAll(
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
    public ApiResponse<SupplierResponse> create(
            @Valid @RequestBody CreateSupplierRequest request
    ) {
        return ApiResponse.ok(service.create(request));
    }

    @GetMapping("/{code}")
    public ApiResponse<SupplierResponse> findByCode(
            @PathVariable("code") String code
    ) {
        return ApiResponse.ok(
                service.findByCode(code)
        );
    }

    @PutMapping("/{code}")
    public ApiResponse<SupplierResponse> update(
            @PathVariable("code") String code,
            @Valid @RequestBody UpdateSupplierRequest request
    ) {
        return ApiResponse.ok(
                service.update(code, request)
        );
    }

    @PostMapping("/{code}/contacts")
    public ApiResponse<SupplierResponse> addContact(
            @PathVariable("code") String code,
            @Valid @RequestBody AddSupplierContactRequest request
    ) {
        return ApiResponse.ok(
                service.addContact(code, request)
        );
    }

    @PutMapping("/{code}/contacts/{contactId}")
    public ApiResponse<SupplierResponse> updateContact(
            @PathVariable("code") String code,
            @PathVariable("contactId") UUID contactId,
            @Valid @RequestBody UpdateSupplierContactRequest request
    ) {
        return ApiResponse.ok(
                service.updateContact(code, contactId, request)
        );
    }

    @DeleteMapping("/{code}/contacts/{contactId}")
    public ApiResponse<SupplierResponse> deactivateContact(
            @PathVariable("code") String code,
            @PathVariable("contactId") UUID contactId
    ) {
        return ApiResponse.ok(
                service.deactivateContact(code, contactId)
        );
    }

}