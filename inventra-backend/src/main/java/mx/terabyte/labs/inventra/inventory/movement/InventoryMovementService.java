package mx.terabyte.labs.inventra.inventory.movement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.terabyte.labs.inventra.inventory.movement.dto.InventoryMovementResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryMovementService {

    private final InventoryMovementRepository repository;
    private final InventoryMovementMapper mapper;

    @Transactional(readOnly = true)
    public List<InventoryMovementResponse> findAll() {
        log.debug("Fetching all inventory movements");

        return repository.findAllWithDetails()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

}