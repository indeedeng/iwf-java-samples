package io.github.cadenceoss.iwf.config;

import com.fasterxml.jackson.databind.Module;
import io.github.cadenceoss.iwf.dsl.utils.WorkflowIdGenerator;
import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.mapper.WorkflowModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class DslConfig {

    @Bean
    public Map<String, Workflow> workflows(@Value("classpath:dsl") final Resource dslFileDir) throws IOException {
        try (Stream<Path> pathStream = Files.find(Paths.get(dslFileDir.getURI()),
                Integer.MAX_VALUE,
                (filePath, fileAttr) -> fileAttr.isRegularFile() && filePath.getFileName().toString().matches(".*workflow\\.[a-z]+"))) {
            return pathStream
                    .map(p -> Workflow.fromSource(readFileToString(new File(p.toUri()))))
                    .collect(Collectors.toMap(WorkflowIdGenerator::generateDynamicWfId, Function.identity()));
        }
    }

    @Bean
    public Module workflowModule() {
        return new WorkflowModule();
    }

    private String readFileToString(final File f) {
        try {
            return new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
