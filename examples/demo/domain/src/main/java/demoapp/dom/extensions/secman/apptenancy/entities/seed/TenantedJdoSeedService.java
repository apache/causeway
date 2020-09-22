package demoapp.dom.extensions.secman.apptenancy.entities.seed;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;

import demoapp.dom._infra.seed.SeedServiceAbstract;
import demoapp.dom._infra.samples.NameSamples;
import demoapp.dom.extensions.secman.apptenancy.entities.TenantedJdo;

@Service
public class TenantedJdoSeedService extends SeedServiceAbstract {

    public TenantedJdoSeedService() {
        super(TenantedJdoEntityFixture::new);
    }

    static class TenantedJdoEntityFixture extends FixtureScript {

        @Override
        protected void execute(ExecutionContext executionContext) {
            secManNameSamples.stream()
                    .map(TenantedJdo::new)
                    .forEach(domainObject -> {
                        repositoryService.persist(domainObject);
                        executionContext.addResult(this, domainObject);
                    });
        }

        @Inject RepositoryService repositoryService;
        @Inject NameSamples secManNameSamples;
    }

}
