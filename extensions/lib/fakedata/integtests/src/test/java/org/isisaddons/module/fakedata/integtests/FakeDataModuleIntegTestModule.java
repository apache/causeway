package org.isisaddons.module.fakedata.integtests;

import java.util.Set;

import org.isisaddons.module.fakedata.FakeDataModule;
import org.isisaddons.module.fakedata.fixture.FakeDataFixturesModule;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.google.common.collect.Sets;

@Configuration
@Import({
        FakeDataModule.class,
        FakeDataFixturesModule.class
})
public class FakeDataModuleIntegTestModule {}
