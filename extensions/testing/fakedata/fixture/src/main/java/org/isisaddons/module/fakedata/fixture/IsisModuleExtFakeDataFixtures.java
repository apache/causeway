package org.isisaddons.module.fakedata.fixture;

import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript;
import org.apache.isis.extensions.fixtures.modules.ModuleWithFixtures;
import org.apache.isis.extensions.fakedata.dom.IsisModuleExtFakeData;
import org.isisaddons.module.fakedata.fixture.demoapp.demomodule.fixturescripts.FakeDataDemoObjectWithAll_tearDown;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        IsisModuleExtFakeData.class
})
@ComponentScan
public class IsisModuleExtFakeDataFixtures implements ModuleWithFixtures {

    @Override public FixtureScript getTeardownFixture() {
        return new FakeDataDemoObjectWithAll_tearDown();
    }

}
