package demoapp.dom.viewmodels.jaxbrefentity.seed;

import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.core.runtime.events.app.AppLifecycleEvent;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;

import demoapp.dom._infra.samples.NameSamples;
import demoapp.dom._infra.seed.SeedServiceAbstract;
import demoapp.dom.viewmodels.jaxbrefentity.ChildJdo;

@Service
public class ChildJdoSeedService extends SeedServiceAbstract {

    public ChildJdoSeedService() {
        super(ChildJdoEntityFixture::new);
    }

    static class ChildJdoEntityFixture extends FixtureScript {

        @Override
        protected void execute(ExecutionContext executionContext) {
            nameSamples.stream()
                    .map(ChildJdo::new)
                    .forEach(domainObject -> {
                        repositoryService.persist(domainObject);
                        executionContext.addResult(this, domainObject);
                    });
        }

        @Inject
        RepositoryService repositoryService;

        @Inject
        NameSamples nameSamples;

    }
}
