package demoapp.dom.annotDomain.DomainObject.publishing;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;

import demoapp.dom._infra.seed.SeedServiceAbstract;
import demoapp.dom.types.Samples;

@Service
public class DomainObjectPublishingDisabledJdoSeedService extends SeedServiceAbstract {

    public DomainObjectPublishingDisabledJdoSeedService() {
        super(PropertyPublishingNotJdoEntityFixture::new);
    }

    static class PropertyPublishingNotJdoEntityFixture extends FixtureScript {

        @Override
        protected void execute(ExecutionContext executionContext) {
            samples.stream()
                    .map(DomainObjectPublishingDisabledJdo::new)
                    .forEach(domainObject -> {
                        repositoryService.persist(domainObject);
                        executionContext.addResult(this, domainObject);
                    });

        }

        @Inject
        RepositoryService repositoryService;

        @Inject
        Samples<String> samples;
    }
}
