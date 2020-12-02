package com.monepic.tradeprice.model.requests;

import com.monepic.tradeprice.model.Instrument;
import com.monepic.tradeprice.model.Vendor;
import com.monepic.tradeprice.model.VendorInstrumentPrice;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public class NewPriceRequest {

    public NewPriceRequest() {}

    public NewPriceRequest(Long vendorId, String vendorName, String vendorDescription,
                           String instrumentSymbol, String instrumentDescription,
                           BigDecimal bid, BigDecimal ask) {
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.vendorDescription = vendorDescription;
        this.instrumentSymbol = instrumentSymbol;
        this.instrumentDescription = instrumentDescription;
        this.bid = bid;
        this.ask = ask;
    }

    @NotNull
    private Long vendorId;
    @NotBlank
    private String vendorName, vendorDescription;

    @NotBlank
    private String instrumentSymbol, instrumentDescription;

    @NotNull
    private BigDecimal bid, ask;

    private Instant timestamp;

    public Long getVendorId() { return vendorId; }

    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

    public String getVendorName() { return vendorName; }

    public void setVendorName(String vendorName) { this.vendorName = vendorName; }

    public String getVendorDescription() { return vendorDescription; }

    public void setVendorDescription(String vendorDescription) { this.vendorDescription = vendorDescription; }

    public String getInstrumentSymbol() { return instrumentSymbol; }

    public void setInstrumentSymbol(String instrumentSymbol) { this.instrumentSymbol = instrumentSymbol; }

    public String getInstrumentDescription() { return instrumentDescription; }

    public void setInstrumentDescription(String instrumentDescription) { this.instrumentDescription = instrumentDescription; }

    public BigDecimal getBid() { return bid; }

    public void setBid(BigDecimal bid) { this.bid = bid; }

    public BigDecimal getAsk() { return ask; }

    public void setAsk(BigDecimal ask) { this.ask = ask; }

    public Instant getTimestamp() { return timestamp; }

    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "NewPriceRequest{" +
                "vendorId=" + vendorId +
                ", vendorName='" + vendorName + '\'' +
                ", vendorDescription='" + vendorDescription + '\'' +
                ", instrumentSymbol='" + instrumentSymbol + '\'' +
                ", instrumentDescription='" + instrumentDescription + '\'' +
                ", bid=" + bid +
                ", ask=" + ask +
                ", timestamp=" + timestamp +
                '}';
    }

    public VendorInstrumentPrice toVendorInstrumentPrice() {
        Vendor v = new Vendor(vendorId, vendorName, vendorDescription);
        Instrument i = new Instrument(instrumentSymbol, instrumentDescription);
        return new VendorInstrumentPrice(v, i, bid, ask, timestamp == null ? Instant.now() : timestamp);
    }
}
