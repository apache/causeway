package org.apache.isis.runtime.boot;

import lombok.extern.log4j.Log4j2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

@Log4j2
public class LoggingSpringApplicationRunListener implements SpringApplicationRunListener {
    private SpringApplication app;
    private String[] args;

    public LoggingSpringApplicationRunListener(SpringApplication app, String[] args){
        this.app = app;
        this.args = args;
    }

    @Override
    public void starting() {
        log.debug("starting");
    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {
        log.debug("environmentPrepared");
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        log.debug("contextPrepared");
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        log.debug("contextLoaded");
    }

    @Override
    public void started(ConfigurableApplicationContext context) {
        log.debug("started");
    }

    @Override
    public void running(ConfigurableApplicationContext context) {
        log.debug("running");
    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        log.error("failed", exception);
    }

}