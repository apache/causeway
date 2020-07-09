package demoapp.dom._infra.seed;

import java.util.function.Supplier;

import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;

import demoapp.dom._infra.fixtures.DemoFixtureScript;


public interface SeedService {

    void seed(FixtureScript fixtureScriptSupplier, FixtureScript.ExecutionContext executionContext);

}
