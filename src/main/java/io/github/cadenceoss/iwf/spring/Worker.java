package io.github.cadenceoss.iwf.spring;

import org.springframework.boot.SpringApplication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Worker {
    
    ExecutorService executor = Executors.newSingleThreadExecutor();

    public void start() {
        System.getProperties().put( "server.port", 8181 );  //8181 port is set here
        executor.submit(() -> {
            SpringApplication.run(SpringMainApplication.class);
        });
    }

    public void stop() {
        executor.shutdown();
    }
}
