package org.apache.isis.webapp.health;

import lombok.extern.log4j.Log4j2;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.isis.applib.services.health.HealthCheckService;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

@Component
@Named("isisWebapp.HealthIndicatorUsingHealthCheckService")
@Log4j2
public class HealthIndicatorUsingHealthCheckService extends AbstractHealthIndicator {

    private final Optional<HealthCheckService> healthCheckServiceIfAny;

    @Inject
    public HealthIndicatorUsingHealthCheckService(final Optional<HealthCheckService> healthCheckServiceIfAny) {
        this.healthCheckServiceIfAny = healthCheckServiceIfAny;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        final Boolean result = healthCheckServiceIfAny.map(HealthCheckService::check)
                .map(org.apache.isis.applib.services.health.Health::getResult)
                .orElse(null);
        if(result != null) {
            if(result) {
                builder.up();
            } else {
                builder.down();
            }
        } else {
            builder.unknown();
        }
    }
}
