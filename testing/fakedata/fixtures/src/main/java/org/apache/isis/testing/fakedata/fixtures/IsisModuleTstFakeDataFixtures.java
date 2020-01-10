package org.apache.isis.testing.fakedata.fixtures;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.extensions.fakedata.dom.IsisModuleExtFakeData;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fixtures.applib.modules.ModuleWithFixtures;
import org.apache.isis.testing.fakedata.fixtures.demoapp.demomodule.fixturescripts.FakeDataDemoObjectWithAll_tearDown;

@Configuration
@Import({
        IsisModuleExtFakeData.class
})
@ComponentScan
public class IsisModuleTstFakeDataFixtures implements ModuleWithFixtures {

    @Override public FixtureScript getTeardownFixture() {
        return new FakeDataDemoObjectWithAll_tearDown();
    }

}
