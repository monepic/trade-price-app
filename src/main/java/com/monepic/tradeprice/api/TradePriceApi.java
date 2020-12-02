package com.monepic.tradeprice.api;

import com.monepic.tradeprice.model.VendorInstrumentPrice;
import com.monepic.tradeprice.model.requests.NewPriceRequest;
import com.monepic.tradeprice.service.TradePriceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;

@RestController
public class TradePriceApi {

    private final TradePriceService tradePriceService;

    public TradePriceApi(TradePriceService tradePriceService) {
        this.tradePriceService = tradePriceService;
    }

    @PostMapping(value = "/price", produces = "application/json", consumes = "application/json")
    public ResponseEntity<?> createPrice(@RequestBody @Valid NewPriceRequest newPriceRequest) {
        tradePriceService.createOrUpdate(newPriceRequest.toVendorInstrumentPrice());
        return ResponseEntity.accepted().build();
    }

    @GetMapping(value = "/price", produces = "application/json")
    public Collection<VendorInstrumentPrice> getAll() {
        return tradePriceService.getAll();
    }

    @GetMapping(value = "/vendor/{id}/prices", produces = "application/json")
    public Collection<VendorInstrumentPrice> getByVendor(@PathVariable Long id) {
        return tradePriceService.getByVendor(id);
    }

    @GetMapping(value = "/instrument/{symbol}/prices", produces = "application/json")
    public Collection<VendorInstrumentPrice> getByInstrument(@PathVariable String symbol) {
        return tradePriceService.getByInstrument(symbol);
    }
}
