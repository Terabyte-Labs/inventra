package mx.terabyte.labs.inventra.catalog.unit;

import lombok.RequiredArgsConstructor;
import mx.terabyte.labs.inventra.catalog.unit.dto.UnitOfMeasureResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UnitOfMeasureService {

    private final UnitOfMeasureRepository repository;

    public List<UnitOfMeasureResponse> findAll() {
        return repository.findAll()
            .stream()
            .map(unit -> new UnitOfMeasureResponse(
                unit.getId(),
                unit.getCode(),
                unit.getName()
            ))
            .toList();
    }
}