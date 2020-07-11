package demoapp.dom.types.jodatime.jodadatetime.jdo;

import javax.inject.Inject;

import org.joda.time.DateTime;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.core.runtime.events.app.AppLifecycleEvent;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;

import demoapp.dom._infra.seed.SeedServiceAbstract;
import demoapp.dom.types.Samples;

@Service
public class JodaDateTimeJdoSeedService extends SeedServiceAbstract {

    public JodaDateTimeJdoSeedService() {
        super(JodaDateTimeJdoEntityFixture::new);
    }

    static class JodaDateTimeJdoEntityFixture extends FixtureScript {

        @Override
        protected void execute(ExecutionContext executionContext) {
            samples.stream()
                    .map(JodaDateTimeJdo::new)
                    .forEach(domainObject -> {
                        repositoryService.persist(domainObject);
                        executionContext.addResult(this, domainObject);
                    });

        }

        @Inject
        RepositoryService repositoryService;

        @Inject
        Samples<DateTime> samples;
    }
}
