package com.monepic.tradeprice.integration;

import com.monepic.tradeprice.model.requests.NewPriceRequest;
import com.monepic.tradeprice.service.TradePriceService;
import com.monepic.tradeprice.utils.TotallyNaiveCsvTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.MessageRejectedException;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.filters.ChainFileListFilter;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.jms.dsl.Jms;
import org.springframework.integration.transformer.AbstractTransformer;
import org.springframework.integration.transformer.MapToObjectTransformer;
import org.springframework.integration.transformer.MessageTransformationException;
import org.springframework.integration.transformer.Transformer;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;

import javax.jms.ConnectionFactory;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Set;

@Configuration
public class VendorInputFlows {

    private static final Logger LOG = LoggerFactory.getLogger(VendorInputFlows.class);

    @Value("${inbound.directory:./filesIn}")
    private String inboundDirectory;

    /**
     * This flow looks for CSV files in <b>inboundDirectory</b>, then tokenizes the results
     * and converts them to {@link NewPriceRequest} objects, and sends them to the
     * tradePriceServiceActivator
     */
    @Bean
    public IntegrationFlow csvInputFlow() {
        return IntegrationFlows
                .from(Files.inboundAdapter(new File(inboundDirectory))
                        .filter(new ChainFileListFilter<File>()
                                .addFilter(new AcceptOnceFileListFilter<>())
                                .addFilter(new SimplePatternFileListFilter("*.csv"))
                        ), e -> e.poller(Pollers.fixedDelay(1000)))
                .split(Files.splitter()
                        .charset(StandardCharsets.UTF_8)
                        .firstLineAsHeader("fileHeader")
                        .applySequence(true))
                .transform(csvToMapTransformer())
                .transform(new MapToObjectTransformer(NewPriceRequest.class))
                .transform(setTimestampFromHeaderEnricher())
                .log(LoggingHandler.Level.DEBUG)
                .channel(c -> c.direct("newPriceRequestChannel"))
                .get();
    }

    @Bean
    public IntegrationFlow jmsInputFlow(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        return IntegrationFlows.from(
                Jms.messageDrivenChannelAdapter(connectionFactory)
                        .jmsMessageConverter(messageConverter)
                        .destination("new-price-in")
        ).log()
                .transform(setTimestampFromHeaderEnricher())
                .channel("newPriceRequestChannel")
                .get();
    }

    /**
     * Service Activator to connect the incoming message flows to our TradePriceService.
     * Invalid new prices are rejected by the validator.
     */
    @Bean
    @ServiceActivator(inputChannel = "newPriceRequestChannel")
    public MessageHandler tradePriceServiceActivator(TradePriceService tps, Validator validator) {
        return (msg) -> {
            Object payload = msg.getPayload();
            if (payload instanceof NewPriceRequest) {
                NewPriceRequest npr = (NewPriceRequest) payload;
                Set<ConstraintViolation<NewPriceRequest>> errs = validator.validate(npr);
                if (!errs.isEmpty()) {
                    throw new MessageRejectedException(msg, errs.toString());
                }
                tps.createOrUpdate(npr.toVendorInstrumentPrice());
            }
        };
    }

    @Bean
    public PublishSubscribeChannel errorChannel() {
        return new PublishSubscribeChannel();
    }

    /**
     * Log the errors without the stacktrace
     */
    @Bean
    @ServiceActivator(inputChannel = "errorChannel")
    public MessageHandler errHandler() {
        return msg -> LOG.error("{}", msg);
    }

    /**
     * turn a row of the incoming CSV file into a map
     */
    private Transformer csvToMapTransformer() {
        return new AbstractTransformer() {
            @Override
            public Object doTransform(Message msg) {
                Object header = msg.getHeaders().get("fileHeader");
                if (header == null) {
                    throw new MessageTransformationException(msg, "no 'fileHeader' header found");
                }
                return new TotallyNaiveCsvTokenizer(
                        header.toString())
                        .tokenize(msg.getPayload().toString());
            }
        };
    }

    /**
     * use the message timestamp, if none was provided in the incoming file data
     */
    private Transformer setTimestampFromHeaderEnricher() {
        return new AbstractTransformer() {
            @Override
            public Object doTransform(Message msg) {
                Object payload = msg.getPayload();
                Long timestamp = msg.getHeaders().getTimestamp();
                if (payload instanceof NewPriceRequest && timestamp != null) {
                    ((NewPriceRequest) payload).setTimestamp(Instant.ofEpochMilli(timestamp));
                }
                return payload;
            }
        };
    }
}
