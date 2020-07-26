package demoapp.dom.annotDomain.Property.domainEvent;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.events.domain.PropertyDomainEvent;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;

import demoapp.dom._infra.seed.SeedServiceAbstract;
import demoapp.dom.types.Samples;
import demoapp.dom.types.javalang.strings.jdo.JavaLangStringJdo;

@Service
public class PropertyDomainEventJdoSeedService extends SeedServiceAbstract {

    public PropertyDomainEventJdoSeedService() {
        super(PropertyDomainEventJdoEntityFixture::new);
    }

    static class PropertyDomainEventJdoEntityFixture extends FixtureScript {

        @Override
        protected void execute(ExecutionContext executionContext) {
            samples.stream()
                    .map(PropertyDomainEventJdo::new)
                    .forEach(domainObject -> {
                        repositoryService.persistAndFlush(domainObject);
                        executionContext.addResult(this, domainObject);
                    });

        }

        @Inject
        RepositoryService repositoryService;

        @Inject
        Samples<String> samples;
    }
}
