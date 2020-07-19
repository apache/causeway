package demoapp.dom.types.jodatime.jodalocaldatetime.jdo;

import javax.inject.Inject;

import org.joda.time.LocalDateTime;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;

import demoapp.dom._infra.seed.SeedServiceAbstract;
import demoapp.dom.types.Samples;

@Service
public class JodaLocalDateTimeJdoSeedService extends SeedServiceAbstract {

    public JodaLocalDateTimeJdoSeedService() {
        super(JodaLocalDateTimeJdoEntityFixture::new);
    }

    static class JodaLocalDateTimeJdoEntityFixture extends FixtureScript {

        @Override
        protected void execute(ExecutionContext executionContext) {
            samples.stream()
                    .map(JodaLocalDateTimeJdo::new)
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
