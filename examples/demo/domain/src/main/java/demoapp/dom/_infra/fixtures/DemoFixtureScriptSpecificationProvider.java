package demoapp.dom._infra.fixtures;

import org.springframework.stereotype.Component;

import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;
import org.apache.isis.testing.fixtures.applib.fixturespec.FixtureScriptsSpecification;
import org.apache.isis.testing.fixtures.applib.fixturespec.FixtureScriptsSpecificationProvider;

import lombok.val;

@Component
public class DemoFixtureScriptSpecificationProvider implements FixtureScriptsSpecificationProvider {

    @Override
    public FixtureScriptsSpecification getSpecification() {
        return new FixtureScriptsSpecification(
                getClass().getPackage().getName()
                , FixtureScripts.NonPersistedObjectsStrategy.IGNORE
                , FixtureScripts.MultipleExecutionStrategy.EXECUTE_ONCE_BY_VALUE
                , DemoFixtureScript.class
                , DemoFixtureScript.class
        );
    }
}
