package demoapp.dom._infra.fixtures;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;

import demoapp.dom._infra.seed.SeedService;

public class DemoFixtureScript extends FixtureScript {

    @Override
    protected void execute(ExecutionContext executionContext) {
        serviceRegistry
                .select(SeedService.class)  // lookup rather than injection to avoid circular reference.
                .forEach(seedService -> seedService.seed(this, executionContext));
    }

    @Inject
    ServiceRegistry serviceRegistry;
}
