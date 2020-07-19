package demoapp.dom.types.javatime.javatimelocaldatetime.jdo;

import java.time.LocalDateTime;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;

import demoapp.dom._infra.seed.SeedServiceAbstract;
import demoapp.dom.types.Samples;

@Service
public class JavaTimeLocalDateTimeJdoSeedService extends SeedServiceAbstract {

    public JavaTimeLocalDateTimeJdoSeedService() {
        super(TemporalJavaTimeLocalDateTimeJdoEntityFixture::new);
    }

    static class TemporalJavaTimeLocalDateTimeJdoEntityFixture extends FixtureScript {

        @Override
        protected void execute(ExecutionContext executionContext) {
            samples.stream()
                    .map(JavaTimeLocalDateTimeJdo::new)
                    .forEach(domainObject -> {
                        repositoryService.persist(domainObject);
                        executionContext.addResult(this, domainObject);
                    });

        }

        @Inject
        RepositoryService repositoryService;

        @Inject
        Samples<LocalDateTime> samples;
    }
}
