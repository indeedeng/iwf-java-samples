package io.iworkflow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iworkflow.core.Client;
import io.iworkflow.core.ClientOptions;
import io.iworkflow.core.JacksonJsonObjectEncoder;
import io.iworkflow.core.Registry;
import io.iworkflow.core.UntypedClient;
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
    public UntypedClient untypedClient() {
        return new UntypedClient(ClientOptions.minimum("http://localhost:8080", ClientOptions.defaultServerUrl));
    }

    @Bean
    public Client client(Registry registry) {
        return new Client(registry, ClientOptions.minimum("http://localhost:8080", ClientOptions.defaultServerUrl));
    }
}
