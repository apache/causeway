package org.isisaddons.module.fakedata.integtests;

import org.isisaddons.module.fakedata.fixture.FakeDataFixturesModule;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        FakeDataFixturesModule.class
})
@ComponentScan
public class FakeDataModuleIntegTestModule {}
