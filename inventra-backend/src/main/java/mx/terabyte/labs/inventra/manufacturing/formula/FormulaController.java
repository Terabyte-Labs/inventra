package mx.terabyte.labs.inventra.manufacturing.formula;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mx.terabyte.labs.inventra.common.api.ApiResponse;
import mx.terabyte.labs.inventra.manufacturing.formula.dto.CreateFormulaRequest;
import mx.terabyte.labs.inventra.manufacturing.formula.dto.CreateFormulaResponse;
import mx.terabyte.labs.inventra.manufacturing.formula.dto.FormulaDetailResponse;
import mx.terabyte.labs.inventra.manufacturing.formula.dto.FormulaSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/manufacturing/formulas")
@RequiredArgsConstructor
public class FormulaController {

    private final FormulaService formulaService;

    @PostMapping
    public ApiResponse<FormulaDetailResponse> create(
            @Valid @RequestBody CreateFormulaRequest request
    ) {
        return ApiResponse.ok(
                formulaService.create(request)
        );
    }

    @GetMapping
    public ApiResponse<Page<FormulaSearchResponse>> findAll(
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "outputProductSku", required = false) String outputProductSku,
            @PageableDefault(
                    size = 20,
                    sort = "code",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        return ApiResponse.ok(
                formulaService.findAll(
                        code,
                        name,
                        outputProductSku,
                        pageable
                )
        );
    }

    @GetMapping("/{code}/versions/{version}")
    public ApiResponse<FormulaDetailResponse> findByCodeAndVersion(
            @PathVariable("code") String code,
            @PathVariable("version") Integer version
    ) {
        return ApiResponse.ok(
                formulaService.findByCodeAndVersion(code, version)
        );
    }
}