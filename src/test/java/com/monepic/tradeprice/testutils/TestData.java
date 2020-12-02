package com.monepic.tradeprice.testutils;

import com.monepic.tradeprice.model.Instrument;
import com.monepic.tradeprice.model.Vendor;
import com.monepic.tradeprice.model.VendorInstrumentPrice;
import com.monepic.tradeprice.model.requests.NewPriceRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.function.Supplier;

public final class TestData {
    public static Vendor VENDOR_1 = new Vendor(1L, "Vendor 1", "First Vendor");
    public static Vendor VENDOR_2 = new Vendor(2L, "Vendor 2", "Second Vendor");

    public static Instrument INSTRUMENT_VOD = new Instrument("VOD", "Vodafone");
    public static Instrument INSTRUMENT_BAY = new Instrument("BAY", "British Airways");

    public static Instant TIMESTAMP_1 = Instant.parse("2015-11-27T00:00:00.00Z");

    public static Supplier<VendorInstrumentPrice> VIP_1 = () -> new VendorInstrumentPrice(VENDOR_1, INSTRUMENT_VOD,
            BigDecimal.valueOf(10.21), BigDecimal.valueOf(11.21), TIMESTAMP_1);

    public static Supplier<VendorInstrumentPrice> VIP_2 = () -> new VendorInstrumentPrice(VENDOR_1, INSTRUMENT_BAY,
            BigDecimal.valueOf(11), BigDecimal.valueOf(12), TIMESTAMP_1);

    public static Supplier<VendorInstrumentPrice> VIP_3 = () -> new VendorInstrumentPrice(VENDOR_2, INSTRUMENT_BAY,
            BigDecimal.valueOf(10.5), BigDecimal.valueOf(11.5), TIMESTAMP_1);

    public static String VIP_1_JSON = "{\"vendor\":{\"id\":1,\"name\":\"Vendor 1\",\"description\":\"First Vendor\"},\"instrument\":{\"symbol\":\"VOD\",\"description\":\"Vodafone\"},\"bid\":10.21,\"ask\":11.21,\"timestamp\":\"2015-11-27T00:00:00Z\"}";

    public static String NPR_1_JSON =
            "{\"vendorId\":1,\"vendorName\":\"Vendor 1\",\"vendorDescription\":\"First Vendor\",\"instrumentSymbol\":\"VOD\",\"instrumentDescription\":\"Vodafone\",\"bid\":10.21,\"ask\":11.21}";

    public static NewPriceRequest NPR_1 =
            new NewPriceRequest(1L, "Vendor 1", "First Vendor",
                    "VOD", "Vodafone",
                    new BigDecimal("10.21"), new BigDecimal("11.21"));


}
