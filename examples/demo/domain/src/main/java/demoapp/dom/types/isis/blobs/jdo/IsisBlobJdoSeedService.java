package demoapp.dom.types.isis.blobs.jdo;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;

import demoapp.dom._infra.seed.SeedServiceAbstract;
import demoapp.dom.types.Samples;

@Service
public class IsisBlobJdoSeedService extends SeedServiceAbstract {

    public IsisBlobJdoSeedService() {
        super(IsisBlobJdoEntityFixture::new);
    }


    static class IsisBlobJdoEntityFixture extends FixtureScript {

        @Override
        protected void execute(ExecutionContext executionContext) {
            samples.stream()
                    .map(IsisBlobJdo::new)
                    .forEach(domainObject -> {
                        repositoryService.persist(domainObject);
                        executionContext.addResult(this, domainObject);
                    });
        }

        @Inject RepositoryService repositoryService;
        @Inject Samples<Blob> samples;
    }
}
