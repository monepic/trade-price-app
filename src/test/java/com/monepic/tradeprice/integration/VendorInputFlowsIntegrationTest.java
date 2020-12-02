package com.monepic.tradeprice.integration;

import com.monepic.tradeprice.config.JmsConfig;
import com.monepic.tradeprice.model.VendorInstrumentPrice;
import com.monepic.tradeprice.service.TradePriceService;
import com.monepic.tradeprice.testutils.TestData;
import com.monepic.tradeprice.testutils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.integration.IntegrationAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.transformer.MessageTransformationException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import javax.jms.JMSException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.monepic.tradeprice.testutils.TestUtils.copyFile;
import static com.monepic.tradeprice.testutils.TestUtils.countDown;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = {IntegrationAutoConfiguration.class, ActiveMQAutoConfiguration.class, JmsAutoConfiguration.class,
        JmsConfig.class, TestUtils.TestContext.class, VendorInputFlows.class,},
        properties = {"spring.main.banner-mode=off", "logging.level.org.springframework.integration=DEBUG"})
@ContextConfiguration(initializers = TestUtils.Initializer.class)
@DirtiesContext
public class VendorInputFlowsIntegrationTest {

    @Value("${inbound.directory}")
    private String inboundDirectory;

    @Autowired
    @Qualifier("errorChannel")
    private PublishSubscribeChannel errorChannel;

    @MockBean
    private TradePriceService mockTradePriceService;

    @Qualifier("jmsTemplate")
    @Autowired
    JmsTemplate jms;

    @AfterEach
    public void cleanup() {
        for (File file : new File(inboundDirectory).listFiles()) {
            file.delete();
        }
    }

    @Test
    public void testHappyPath() throws InterruptedException {

        int expectedMessageCount = 4;
        CountDownLatch latch = new CountDownLatch(expectedMessageCount);
        ArgumentCaptor<VendorInstrumentPrice> captor = ArgumentCaptor.forClass(VendorInstrumentPrice.class);

        countDown(latch)
                .when(mockTradePriceService).createOrUpdate(captor.capture());

        // provoke the VendorInputFlow into processing a file
        copyFile("test_good_file.csv", inboundDirectory);

        latch.await(10, TimeUnit.SECONDS);

        //got the right number of messages
        verify(mockTradePriceService, times(4)).createOrUpdate(any());
        //and credible data
        assertEquals("British Airways", captor.getAllValues().get(3).getInstrument().getDescription());
    }

    @Test
    public void testErrorChannel() throws InterruptedException {

        int expectedMessageCount = 1;
        CountDownLatch latch = new CountDownLatch(expectedMessageCount);
        List<Message<?>> results = new ArrayList<>();
        errorChannel.subscribe((msg) -> {
                    results.add(msg);
                    latch.countDown();
                }
        );

        // provoke the VendorInputFlow into processing a file
        copyFile("test_bad_file.csv", inboundDirectory);
        latch.await(10, TimeUnit.SECONDS);

        assertEquals(1, results.size());
        assertEquals(MessageTransformationException.class, results.get(0).getPayload().getClass());
    }

    @Test
    public void testJmsInput() throws JMSException, InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);
        ArgumentCaptor<VendorInstrumentPrice> captor = ArgumentCaptor.forClass(VendorInstrumentPrice.class);
        countDown(latch)
                .when(mockTradePriceService).createOrUpdate(captor.capture());

       jms.convertAndSend("new-price-in", TestData.NPR_1);

        boolean timedOut = latch.await(10, TimeUnit.SECONDS);
        assertEquals(1, captor.getAllValues().size());
    }
}