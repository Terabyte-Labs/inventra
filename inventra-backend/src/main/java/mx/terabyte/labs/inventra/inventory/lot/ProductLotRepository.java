package mx.terabyte.labs.inventra.inventory.lot;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductLotRepository extends JpaRepository<ProductLotEntity, UUID> {

    Optional<ProductLotEntity> findByProductIdAndLotNumber(
            UUID productId,
            String lotNumber
    );

}