package io.iworkflow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iworkflow.core.Client;
import io.iworkflow.core.ClientOptions;
import io.iworkflow.core.JacksonJsonObjectEncoder;
import io.iworkflow.core.Registry;
import io.iworkflow.core.UnregisteredClient;
import io.iworkflow.core.WorkerOptions;
import io.iworkflow.core.WorkerService;
import io.iworkflow.core.Workflow;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class IwfConfig {
    @Bean
    public Registry registry(Workflow... workflows) {
        Registry registry = new Registry();
        Arrays.stream(workflows).forEach(registry::addWorkflow);
        return registry;
    }

    @Bean
    public WorkerService workerService(final Registry registry, final ObjectMapper mapper) {
        return new WorkerService(registry, WorkerOptions.minimum(new JacksonJsonObjectEncoder(mapper, "BuiltinJacksonJson")));
    }

    @Bean
    public UnregisteredClient unregisteredClient() {
        return new UnregisteredClient(
                ClientOptions.builder()
                        .workerUrl("http://localhost:8080")
                        .serverUrl(ClientOptions.defaultServerUrl)
                        .objectEncoder(new JacksonJsonObjectEncoder())
                        .build()
        );
    }

    @Bean
    public Client client(Registry registry) {
        return new Client(registry,
                ClientOptions.builder()
                        .workerUrl("http://localhost:8080")
                        .serverUrl(ClientOptions.defaultServerUrl)
                        .objectEncoder(new JacksonJsonObjectEncoder())
                        .build()
        );
    }
}
