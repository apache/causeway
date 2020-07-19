package demoapp.dom.types.isis.localresourcepaths.jdo;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.value.LocalResourcePath;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;

import demoapp.dom._infra.seed.SeedServiceAbstract;
import demoapp.dom.types.Samples;

@Service
public class IsisLocalResourcePathJdoSeedService extends SeedServiceAbstract {

    public IsisLocalResourcePathJdoSeedService() {
        super(IsisLocalResourcePathJdoEntityFixture::new);
    }

    static class IsisLocalResourcePathJdoEntityFixture extends FixtureScript {

        @Override
        protected void execute(ExecutionContext executionContext) {
            samples.stream()
                    .map(IsisLocalResourcePathJdo::new)
                    .forEach(domainObject -> {
                        repositoryService.persist(domainObject);
                        executionContext.addResult(this, domainObject);
                    });
        }

        @Inject
        RepositoryService repositoryService;

        @Inject
        Samples<LocalResourcePath> samples;
    }
}
