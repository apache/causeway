package org.apache.isis.core.webapp.health;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.services.health.HealthCheckService;

import lombok.val;

@Component
@Named("isisWebapp.HealthCheckService") // this appears in the endpoint.
public class HealthIndicatorUsingHealthCheckService extends AbstractHealthIndicator {

    private final Optional<HealthCheckService> healthCheckServiceIfAny;

    @Inject
    public HealthIndicatorUsingHealthCheckService(final Optional<HealthCheckService> healthCheckServiceIfAny) {
        this.healthCheckServiceIfAny = healthCheckServiceIfAny;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        val health = healthCheckServiceIfAny.map(HealthCheckService::check)
                     .orElse(null);
        if(health != null) {
            final boolean result = health.getResult();
            if(result) {
                builder.up();
            } else {
                final Throwable cause = health.getCause();
                if(cause != null) {
                    builder.down(cause);
                } else {
                    builder.down();
                }
            }
        } else {
            builder.unknown();
        }
    }
}
