package org.apache.isis.testing.fakedata.fixtures.demoapp.demomodule.fixturescripts;

import org.apache.isis.testing.fixtures.applib.legacy.teardown.TeardownFixtureAbstract2;
import org.apache.isis.testing.fakedata.fixtures.demoapp.demomodule.dom.FakeDataDemoObjectWithAll;

public class FakeDataDemoObjectWithAll_tearDown extends TeardownFixtureAbstract2 {

    @Override
    protected void execute(final ExecutionContext executionContext) {
        deleteFrom(FakeDataDemoObjectWithAll.class);
    }

}
