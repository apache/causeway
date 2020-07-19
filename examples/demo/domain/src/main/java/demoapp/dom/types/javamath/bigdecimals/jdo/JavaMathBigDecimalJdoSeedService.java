package demoapp.dom.types.javamath.bigdecimals.jdo;

import java.math.BigDecimal;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;

import demoapp.dom._infra.seed.SeedServiceAbstract;
import demoapp.dom.types.Samples;

@Service
public class JavaMathBigDecimalJdoSeedService extends SeedServiceAbstract {

    public JavaMathBigDecimalJdoSeedService() {
        super(JavaMathBigDecimalJdoEntityFixture::new);
    }

    static class JavaMathBigDecimalJdoEntityFixture extends FixtureScript {

        @Override
        protected void execute(ExecutionContext executionContext) {
            samples.stream()
                    .map(JavaMathBigDecimalJdo::new)
                    .forEach(domainObject -> {
                        repositoryService.persist(domainObject);
                        executionContext.addResult(this, domainObject);
                    });
        }

        @Inject
        RepositoryService repositoryService;

        @Inject
        Samples<BigDecimal> samples;

    }
}
