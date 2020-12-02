package com.monepic.tradeprice.service;

import com.monepic.tradeprice.cache.InMemoryIndexedCache;
import com.monepic.tradeprice.cache.IndexedCache;
import com.monepic.tradeprice.model.VendorInstrumentPrice;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collection;

public class InMemoryIndexedCacheTradePriceService implements TradePriceService {

    private final IndexedCache<VendorInstrumentPrice> cache = new InMemoryIndexedCache<>(
            vip -> vip.getVendor().getId(),
            vip -> vip.getInstrument().getSymbol()
    );
    private final int VENDOR = 0, INSTRUMENT = 1;

    private final int daysToKeep;

    public InMemoryIndexedCacheTradePriceService(int daysToKeep) {this.daysToKeep = daysToKeep;}

    @Scheduled(cron = "${price.evict.cron}")
    void expireOldPrices() {
        Instant cutoff = ZonedDateTime.now().minusDays(daysToKeep).toInstant();
        cache.evict(vip -> cutoff.isAfter(vip.getTimestamp()));
    }

    @Override
    public void createOrUpdate(VendorInstrumentPrice vip) {
        cache.addItem(vip);
    }

    @Override
    public Collection<VendorInstrumentPrice> getByVendor(Long vendorId) {
        return cache.getByIndexOrdinal(VENDOR, vendorId);
    }

    @Override
    public Collection<VendorInstrumentPrice> getByInstrument(String instrumentSymbol) {
        return cache.getByIndexOrdinal(INSTRUMENT, instrumentSymbol);
    }

    @Override
    public Collection<VendorInstrumentPrice> getAll() {
        return cache.getAll();
    }
}
