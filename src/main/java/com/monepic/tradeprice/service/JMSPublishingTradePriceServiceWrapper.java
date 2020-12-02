package com.monepic.tradeprice.service;

import com.monepic.tradeprice.model.VendorInstrumentPrice;
import org.springframework.jms.core.JmsTemplate;

import java.util.Collection;

public class JMSPublishingTradePriceServiceWrapper implements TradePriceService {

    private final TradePriceService delegate;
    private final JmsTemplate jms;
    private final String jmsDestination;

    public JMSPublishingTradePriceServiceWrapper(TradePriceService delegate, JmsTemplate jms, String jmsDestination) {
        this.delegate = delegate;
        this.jms = jms;
        this.jmsDestination = jmsDestination;
    }

    @Override
    public void createOrUpdate(VendorInstrumentPrice vip) {
        delegate.createOrUpdate(vip);
        jms.convertAndSend(jmsDestination, vip);
    }

    @Override
    public Collection<VendorInstrumentPrice> getByVendor(Long vendorId) {
        return delegate.getByVendor(vendorId);
    }

    @Override
    public Collection<VendorInstrumentPrice> getByInstrument(String instrumentSymbol) {
        return delegate.getByInstrument(instrumentSymbol);
    }

    @Override
    public Collection<VendorInstrumentPrice> getAll() {
        return delegate.getAll();
    }
}
