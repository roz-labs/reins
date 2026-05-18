package io.github.rozlabs.reins.core;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = HarnessRegistryTest.TestConfig.class)
class HarnessRegistryTest {

    @Configuration
    static class TestConfig {
        @Bean
        FakeHarnessV1 fakeHarnessV1() {
            return new FakeHarnessV1();
        }

        @Bean
        HarnessRegistry harnessRegistry(ApplicationContext context) {
            return new HarnessRegistry(context);
        }
    }

    @Configuration
    static class DuplicateConfig {
        @Bean
        FakeHarnessV1 fakeHarnessV1a() {
            return new FakeHarnessV1();
        }

        @Bean
        FakeHarnessV1 fakeHarnessV1b() {
            return new FakeHarnessV1();
        }
    }

    @Harness(name = "test", version = "v1")
    static class FakeHarnessV1 implements ReinsHarness {
        @Override
        public ChatClient build(ChatClient.Builder builder) {
            return builder.build();
        }
    }

    @Autowired
    HarnessRegistry registry;

    @Test
    void registersHarnessBeanByNameAndVersion() {
        assertThat(registry.get("test", "v1"))
            .isPresent()
            .get()
            .isInstanceOf(FakeHarnessV1.class);
    }

    @Test
    void sizeReflectsRegisteredHarnesses() {
        assertThat(registry.size()).isEqualTo(1);
    }

    @Test
    void unknownHarnessReturnsEmpty() {
        assertThat(registry.get("missing", "v1")).isEmpty();
    }

    @Test
    void duplicateRegistrationThrows() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.register(DuplicateConfig.class);
            ctx.refresh();
            assertThatThrownBy(() -> new HarnessRegistry(ctx))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate harness registration")
                .hasMessageContaining("test:v1");
        }
    }
}
