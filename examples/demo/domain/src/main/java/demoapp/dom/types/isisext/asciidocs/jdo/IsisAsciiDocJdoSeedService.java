package demoapp.dom.types.isisext.asciidocs.jdo;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.valuetypes.asciidoc.applib.value.AsciiDoc;

import demoapp.dom._infra.seed.SeedServiceAbstract;
import demoapp.dom.types.Samples;

@Service
public class IsisAsciiDocJdoSeedService extends SeedServiceAbstract {

    public IsisAsciiDocJdoSeedService() {
        super(IsisAsciiDocJdoEntityFixture::new);
    }

    static class IsisAsciiDocJdoEntityFixture extends FixtureScript {

        @Override
        protected void execute(ExecutionContext executionContext) {
            repositoryService.removeAll(IsisAsciiDocJdo.class);
            samples.stream()
                    .map(IsisAsciiDocJdo::new)
                    .forEach(domainObject -> {
                        repositoryService.persist(domainObject);
                        executionContext.addResult(this, domainObject);
                    });
        }

        @Inject
        RepositoryService repositoryService;

        @Inject
        Samples<AsciiDoc> samples;
    }
}
