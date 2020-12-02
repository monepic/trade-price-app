package com.monepic.tradeprice.service;

import com.monepic.tradeprice.model.VendorInstrumentPrice;
import com.monepic.tradeprice.testutils.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InMemoryIndexedCacheTradePriceServiceTest {

    private InMemoryIndexedCacheTradePriceService service;

    @BeforeEach
    void init() {
        service = new InMemoryIndexedCacheTradePriceService(30);
        service.createOrUpdate(TestData.VIP_1.get());
        service.createOrUpdate(TestData.VIP_2.get());
        service.createOrUpdate(TestData.VIP_3.get());
    }

    @Test
    public void testGetAll() {
        assertEquals(3, service.getAll().size());
    }

    @Test
    public void testGetByInstrument() {
        assertEquals(2, service.getByInstrument("BAY").size());
    }

    @Test
    public void testGetByVendor() {
        assertEquals(1, service.getByVendor(2L).size());
    }

    @Test
    public void testUpdateAndEvict() {
        VendorInstrumentPrice vip = TestData.VIP_2.get();
        vip.setTimestamp(Instant.now());
        service.createOrUpdate(vip);

        service.expireOldPrices();

        assertEquals(0, service.getByVendor(2L).size());
        assertEquals(1, service.getByVendor(1L).size());
        assertEquals(1, service.getAll().size());
    }
}
