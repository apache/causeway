package org.apache.isis.testing.fakedata.fixtures.demoapp.demomodule.fixturescripts;

import org.apache.isis.testing.fixtures.applib.teardown.TeardownFixtureAbstract;
import org.apache.isis.testing.fakedata.fixtures.demoapp.demomodule.dom.FakeDataDemoObjectWithAll;

public class FakeDataDemoObjectWithAll_tearDown extends TeardownFixtureAbstract {

    @Override
    protected void execute(final ExecutionContext executionContext) {
        deleteFrom(FakeDataDemoObjectWithAll.class);
    }

}
