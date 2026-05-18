package io.github.rozlabs.reins.core;

import org.springframework.ai.chat.client.ChatClient;

/**
 * Contract for a Reins-managed harness. Implementations define how the underlying
 * ChatClient is configured for a particular harness version.
 */
public interface ReinsHarness {

    /**
     * Configure and return a ChatClient for this harness.
     * Implementations typically set system prompts, advisors, tools, and model options.
     *
     * @param builder a fresh ChatClient.Builder provided by Spring AI
     * @return the configured ChatClient
     */
    ChatClient build(ChatClient.Builder builder);

    /**
     * Convenience method to send a user message and return the text response.
     * Default implementation calls build() each time; harnesses can cache the ChatClient
     * if desired.
     */
    default String ask(String input) {
        throw new UnsupportedOperationException(
            "Override ask() or call build() and use the ChatClient directly"
        );
    }
}
