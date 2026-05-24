package mx.terabyte.labs.inventra.catalog.product.barcode;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductBarcodeRepository extends JpaRepository<ProductBarcodeEntity, UUID> {

    Optional<ProductBarcodeEntity> findByBarcode(String barcode);

}