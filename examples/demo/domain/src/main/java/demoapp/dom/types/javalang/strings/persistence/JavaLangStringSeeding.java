package demoapp.dom.types.javalang.strings.persistence;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;

import demoapp.dom._infra.seed.SeedServiceAbstract;
import demoapp.dom._infra.values.ValueHolderRepository;

@Service
public class JavaLangStringSeeding
extends SeedServiceAbstract {

    protected JavaLangStringSeeding() {
        super(JavaLangStringEntityFixture::new);
    }

    static class JavaLangStringEntityFixture extends FixtureScript {

        @Override
        protected void execute(final ExecutionContext executionContext) {
            entities.seedSamples(domainObject->
                executionContext.addResult(this, domainObject));
        }

        @Inject
        ValueHolderRepository<String, ? extends JavaLangStringEntity> entities;

    }

}
