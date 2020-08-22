package demoapp.dom.annotDomain.DomainObject.publishing.metaAnnot.enabled;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;

import demoapp.dom._infra.seed.SeedServiceAbstract;
import demoapp.dom.types.Samples;

@Service
public class DomainObjectPublishingEnabledMetaAnnotatedJdoSeedService extends SeedServiceAbstract {

    public DomainObjectPublishingEnabledMetaAnnotatedJdoSeedService() {
        super(PropertyPublishingJdoEntityFixture::new);
    }

    static class PropertyPublishingJdoEntityFixture extends FixtureScript {

        @Override
        protected void execute(ExecutionContext executionContext) {
            samples.stream()
                    .map(DomainObjectPublishingEnabledMetaAnnotatedJdo::new)
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
