package com.monepic.tradeprice.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public class VendorInstrumentPrice {

    private Vendor vendor;
    private Instrument instrument;
    private BigDecimal bid, ask;
    private Instant timestamp;

    public VendorInstrumentPrice() {}

    public VendorInstrumentPrice(Vendor vendor, Instrument instrument, BigDecimal bid, BigDecimal ask) {
        this(vendor, instrument, bid, ask, Instant.now());
    }

    public VendorInstrumentPrice(Vendor vendor, Instrument instrument, BigDecimal bid, BigDecimal ask, Instant timestamp) {
        this.vendor = vendor;
        this.instrument = instrument;
        this.bid = bid;
        this.ask = ask;
        this.timestamp = timestamp;
    }

    public Vendor getVendor() { return vendor; }

    public void setVendor(Vendor vendor) { this.vendor = vendor; }

    public Instrument getInstrument() { return instrument; }

    public void setInstrument(Instrument instrument) { this.instrument = instrument; }

    public BigDecimal getBid() { return bid; }

    public void setBid(BigDecimal bid) { this.bid = bid; }

    public BigDecimal getAsk() { return ask; }

    public void setAsk(BigDecimal ask) { this.ask = ask; }

    public Instant getTimestamp() { return timestamp; }

    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VendorInstrumentPrice)) {
            return false;
        }
        VendorInstrumentPrice that = (VendorInstrumentPrice) obj;
        return Objects.equals(vendor, that.vendor)
                && Objects.equals(instrument, that.instrument);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vendor, instrument);
    }

    @Override
    public String toString() {
        return "VendorInstrumentPrice{" +
                "vendor=" + vendor +
                ", instrument=" + instrument +
                ", bid=" + bid +
                ", ask=" + ask +
                ", timestamp=" + timestamp +
                '}';
    }
}


