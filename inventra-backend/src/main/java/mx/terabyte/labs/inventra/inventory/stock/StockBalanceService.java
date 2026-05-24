package mx.terabyte.labs.inventra.inventory.stock;

import lombok.RequiredArgsConstructor;
import mx.terabyte.labs.inventra.inventory.stock.dto.StockBalanceResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockBalanceService {

    private final StockBalanceRepository repository;

    public List<StockBalanceResponse> findAll() {
        return repository.findAllProjected();
    }
}