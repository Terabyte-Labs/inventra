package mx.terabyte.labs.inventra.catalog.unit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.terabyte.labs.inventra.catalog.unit.dto.UnitOfMeasureResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnitOfMeasureService {

    private final UnitOfMeasureRepository repository;

    public List<UnitOfMeasureResponse> findAll() {
        log.debug("Fetching all units of measure");
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