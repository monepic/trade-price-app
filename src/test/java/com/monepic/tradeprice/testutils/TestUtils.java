package com.monepic.tradeprice.testutils;

import org.apache.commons.io.FileUtils;
import org.mockito.stubbing.Stubber;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

import javax.validation.Validation;
import javax.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

import static org.mockito.Mockito.doAnswer;

public class TestUtils {

    /**
     * Initialises the (tmp) file input directory
     * and sets the location as a context property so our SI pipeline can find it
     */
    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        private Path createDirectory() {
            try {
                return Files.createTempDirectory("filesIn");
            } catch (IOException e) {
                throw new UncheckedIOException((e));
            }
        }

        @Override
        public void initialize(ConfigurableApplicationContext ctx) {
            TestPropertyValues.of(
                    "inbound.directory:" + createDirectory().toString()
            ).applyTo(ctx);
        }
    }

    public static class TestContext {
        @Bean
        public Validator validator() { return Validation.buildDefaultValidatorFactory().getValidator(); }
    }

    public static void copyFile(String resourceName, String targetFolder) {
        try {
            ClassPathResource resource = new ClassPathResource(resourceName);
            System.out.printf("copying %s to %s%n", resource.getURL(), targetFolder);
            FileUtils.copyURLToFile(resource.getURL(),
                    new File(targetFolder, resourceName)
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * credit
     * https://github.com/mockito/mockito/issues/1089#issuecomment-733787423
     */
    public static Stubber countDown(CountDownLatch latch) {
        return doAnswer(i -> {
            latch.countDown();
            return null;
        });
    }
}
