package com.monepic.tradeprice.service;

import com.monepic.tradeprice.model.VendorInstrumentPrice;

import java.util.Collection;

public interface TradePriceService {

    void createOrUpdate(VendorInstrumentPrice vip);

    Collection<VendorInstrumentPrice> getByVendor(Long vendorId);

    Collection<VendorInstrumentPrice> getByInstrument(String instrumentSymbol);

    Collection<VendorInstrumentPrice> getAll();

}
