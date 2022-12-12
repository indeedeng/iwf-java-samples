package io.github.cadenceoss.iwf.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cadenceoss.iwf.core.Client;
import io.github.cadenceoss.iwf.core.ClientOptions;
import io.github.cadenceoss.iwf.core.JacksonJsonObjectEncoder;
import io.github.cadenceoss.iwf.core.Registry;
import io.github.cadenceoss.iwf.core.UntypedClient;
import io.github.cadenceoss.iwf.core.WorkerOptions;
import io.github.cadenceoss.iwf.core.WorkerService;
import io.github.cadenceoss.iwf.core.Workflow;
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
