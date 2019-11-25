package org.isisaddons.module.fakedata.fixture.demoapp.demomodule.fixturescripts;

import org.apache.isis.extensions.fixtures.legacy.teardown.TeardownFixtureAbstract2;
import org.isisaddons.module.fakedata.fixture.demoapp.demomodule.dom.FakeDataDemoObjectWithAll;

public class FakeDataDemoObjectWithAll_tearDown extends TeardownFixtureAbstract2 {

    @Override
    protected void execute(final ExecutionContext executionContext) {
        deleteFrom(FakeDataDemoObjectWithAll.class);
    }

}
