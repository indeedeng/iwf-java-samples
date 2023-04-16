package io.iworkflow.config;

import io.iworkflow.core.Client;
import io.iworkflow.core.ClientOptions;
import io.iworkflow.core.JacksonJsonObjectEncoder;
import io.iworkflow.core.ObjectWorkflow;
import io.iworkflow.core.Registry;
import io.iworkflow.core.UnregisteredClient;
import io.iworkflow.core.WorkerOptions;
import io.iworkflow.core.WorkerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class IwfConfig {
    @Bean
    public Registry registry(ObjectWorkflow... workflows) {
        Registry registry = new Registry();
        Arrays.stream(workflows).forEach(registry::addWorkflow);
        return registry;
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
