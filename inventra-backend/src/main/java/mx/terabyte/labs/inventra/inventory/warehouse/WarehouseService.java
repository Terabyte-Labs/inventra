package mx.terabyte.labs.inventra.inventory.warehouse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.terabyte.labs.inventra.inventory.warehouse.dto.WarehouseResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseService {

    private final WarehouseRepository repository;

    @Transactional(readOnly = true)
    public List<WarehouseResponse> findAllActive() {
        log.debug("Fetching active warehouses");

        return repository.findAll()
                .stream()
                .filter(warehouse -> Boolean.TRUE.equals(warehouse.getActive()))
                .sorted(Comparator.comparing(WarehouseEntity::getName))
                .map(warehouse -> new WarehouseResponse(
                        warehouse.getId(),
                        warehouse.getCode(),
                        warehouse.getName(),
                        warehouse.getLocation(),
                        warehouse.getActive()
                ))
                .toList();
    }
}
