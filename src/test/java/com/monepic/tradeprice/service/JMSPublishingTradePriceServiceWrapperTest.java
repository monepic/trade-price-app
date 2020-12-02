package com.monepic.tradeprice.service;

import com.monepic.tradeprice.model.VendorInstrumentPrice;
import com.monepic.tradeprice.testutils.TestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class JMSPublishingTradePriceServiceWrapperTest {

    @Mock
    TradePriceService delegate;
    @Mock
    JmsTemplate jms;
    String destination = "my-destination";

    @Test
    public void testPassesToJms() {
        JMSPublishingTradePriceServiceWrapper wrapper = new JMSPublishingTradePriceServiceWrapper(
                delegate, jms, destination);

        ArgumentCaptor<String> destCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<VendorInstrumentPrice> vipCaptor = ArgumentCaptor.forClass(VendorInstrumentPrice.class);

        wrapper.createOrUpdate(TestData.VIP_1.get());

        verify(jms, times(1)).convertAndSend(destCaptor.capture(), vipCaptor.capture());

        assertEquals(destination, destCaptor.getValue());
        assertEquals(TestData.VIP_1.get(), vipCaptor.getValue());
    }

}