package domainapp.application.services.health;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.services.health.Health;
import org.apache.isis.applib.services.health.HealthCheckService;

import domainapp.modules.simple.dom.impl.SimpleObjects;

@DomainService(nature = NatureOfService.DOMAIN)
public class HealthCheckServiceImpl implements HealthCheckService {

    @Override
    public Health check() {

        try {
            simpleObjects.ping();
            return Health.ok();
        } catch (Exception ex) {
            return Health.error(ex.getMessage());
        }

    }

    @Inject
    SimpleObjects simpleObjects;
}
