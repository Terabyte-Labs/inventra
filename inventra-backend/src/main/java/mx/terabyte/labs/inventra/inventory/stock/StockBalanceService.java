package mx.terabyte.labs.inventra.inventory.stock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.terabyte.labs.inventra.inventory.stock.dto.StockBalanceResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockBalanceService {

    private final StockBalanceRepository repository;

    public List<StockBalanceResponse> findAll() {
        log.debug("Fetching all stock balances");
        return repository.findAllProjected();
    }
}