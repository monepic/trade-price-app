package com.monepic.tradeprice.service;

import com.monepic.tradeprice.model.VendorInstrumentPrice;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryFilteringTradePriceService implements TradePriceService {

    private final ConcurrentHashMap<VendorInstrumentPrice, VendorInstrumentPrice> allPrices = new ConcurrentHashMap<>(1 << 11);
    private final int daysToKeep;

    public InMemoryFilteringTradePriceService(int daysToKeep) {this.daysToKeep = daysToKeep;}

    @Override
    public void createOrUpdate(VendorInstrumentPrice vip) {
        allPrices.put(vip, vip);
    }

    @Override
    public Collection<VendorInstrumentPrice> getByVendor(Long vendorId) {
        return vendorId == null ? Collections.emptySet() :
                allPrices.values()
                        .stream()
                        .filter(vip -> vendorId.equals(vip.getVendor().getId()))
                        .collect(Collectors.toSet());
    }

    @Override
    public Collection<VendorInstrumentPrice> getByInstrument(String instrumentSymbol) {
        return instrumentSymbol == null ? Collections.emptySet() :
                allPrices.values()
                        .stream()
                        .filter(vip -> instrumentSymbol.equals(vip.getInstrument().getSymbol()))
                        .collect(Collectors.toSet());
    }

    @Override
    public Collection<VendorInstrumentPrice> getAll() {
        return new HashSet<>(allPrices.values());
    }

    @Scheduled(cron = "${price.evict.cron}")
    void expireOldPrices() {
        Instant cutoff = ZonedDateTime.now().minusDays(daysToKeep).toInstant();
        allPrices.values().stream()
                .filter(vip -> cutoff.isAfter(vip.getTimestamp()))
                .forEach(v -> {
                    allPrices.remove(v, v); // use the long-form remove(k,v) to avoid removing items that have been concurrently updated
                });
    }
}
