package org.apache.isis.webapp.health;

import lombok.extern.log4j.Log4j2;
import lombok.val;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.isis.applib.services.health.HealthCheckService;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

@Component
@Named("isisWebapp.HealthCheckService") // this appears in the endpoint.
@Log4j2
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
                builder.down(health.getThrowable());
            }
        } else {
            builder.unknown();
        }
    }
}
