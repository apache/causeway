package demoapp.dom.types.javautil.uuids.jdo;

import java.util.UUID;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;

import demoapp.dom._infra.seed.SeedServiceAbstract;
import demoapp.dom.types.Samples;

@Service
public class JavaUtilUuidJdoSeedService extends SeedServiceAbstract {

    public JavaUtilUuidJdoSeedService() {
        super(JavaUtilUuidJdoEntityFixture::new);
    }

    static class JavaUtilUuidJdoEntityFixture extends FixtureScript {

        @Override
        protected void execute(ExecutionContext executionContext) {
            samples.stream()
                    .map(JavaUtilUuidJdo::new)
                    .forEach(domainObject -> {
                        repositoryService.persist(domainObject);
                        executionContext.addResult(this, domainObject);
                    });
        }

        @Inject
        RepositoryService repositoryService;

        @Inject
        Samples<UUID> samples;

    }
}
