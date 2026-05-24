package mx.terabyte.labs.inventra.manufacturing.formula;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mx.terabyte.labs.inventra.common.api.ApiResponse;
import mx.terabyte.labs.inventra.manufacturing.formula.dto.CreateFormulaRequest;
import mx.terabyte.labs.inventra.manufacturing.formula.dto.CreateFormulaResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/manufacturing/formulas")
@RequiredArgsConstructor
public class FormulaController {

    private final FormulaService formulaService;

    @PostMapping
    public ApiResponse<CreateFormulaResponse> create(
            @Valid @RequestBody CreateFormulaRequest request
    ) {
        return ApiResponse.ok(
                formulaService.create(request)
        );
    }
}