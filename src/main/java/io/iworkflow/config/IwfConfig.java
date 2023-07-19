package io.iworkflow.config;

import io.iworkflow.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IwfConfig {
    @Bean
    public Registry registry() {
        return new Registry();
    }

    @Bean
    public WorkerService workerService(final Registry registry) {
        return new WorkerService(registry, WorkerOptions.defaultOptions);
    }

    @Bean
    public UnregisteredClient unregisteredClient(final @Value("${iwf.worker.url}") String workerUrl,
                                                 final @Value("${iwf.server.url}") String serverUrl) {
        return new UnregisteredClient(
                ClientOptions.builder()
                        .workerUrl(workerUrl)
                        .serverUrl(serverUrl)
                        .objectEncoder(new JacksonJsonObjectEncoder())
                        .build()
        );
    }

    @Bean
    public Client client(Registry registry,
                         final @Value("${iwf.worker.url}") String workerUrl,
                         final @Value("${iwf.server.url}") String serverUrl) {
        return new Client(registry,
                ClientOptions.builder()
                        .workerUrl(workerUrl)
                        .serverUrl(serverUrl)
                        .objectEncoder(new JacksonJsonObjectEncoder())
                        .build()
        );
    }
}
