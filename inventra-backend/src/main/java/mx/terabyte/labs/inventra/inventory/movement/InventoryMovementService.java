package mx.terabyte.labs.inventra.inventory.movement;

import lombok.RequiredArgsConstructor;
import mx.terabyte.labs.inventra.inventory.movement.dto.InventoryMovementResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryMovementService {

    private final InventoryMovementRepository repository;

    @Transactional(readOnly = true)
    public List<InventoryMovementResponse> findAll() {
        return repository.findAllProjected();
    }

}