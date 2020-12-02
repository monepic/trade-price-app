package com.monepic.tradeprice.config;

import com.monepic.tradeprice.service.InMemoryFilteringTradePriceService;
import com.monepic.tradeprice.service.InMemoryIndexedCacheTradePriceService;
import com.monepic.tradeprice.service.JMSPublishingTradePriceServiceWrapper;
import com.monepic.tradeprice.service.TradePriceService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class Config {

    @Value("${price.evict.days:30}")
    private int daysToKeep;

    @Bean("tradePriceService")
    @Profile("!filteredCache")
    TradePriceService imIcTradePriceService(@Qualifier("pubSubJmsTemplate") JmsTemplate jms) {
        return new JMSPublishingTradePriceServiceWrapper(
                new InMemoryIndexedCacheTradePriceService(daysToKeep), jms, "new-price-out"
        );
    }

    @Bean("tradePriceService")
    @Profile("filteredCache")
    TradePriceService imfTradePriceService(@Qualifier("pubSubJmsTemplate") JmsTemplate jms) {
        return new JMSPublishingTradePriceServiceWrapper(
                new InMemoryFilteringTradePriceService(daysToKeep), jms, "new-price-out"
        );
    }
}

