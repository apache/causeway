package org.isisaddons.module.fakedata.fixture;

import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript;
import org.apache.isis.extensions.fixtures.modules.Module;
import org.isisaddons.module.fakedata.fixture.demoapp.demomodule.fixturescripts.FakeDataDemoObjectWithAll_tearDown;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FakeDataFixturesModule implements Module {

    @Override public FixtureScript getTeardownFixture() {
        return new FakeDataDemoObjectWithAll_tearDown();
    }

}
