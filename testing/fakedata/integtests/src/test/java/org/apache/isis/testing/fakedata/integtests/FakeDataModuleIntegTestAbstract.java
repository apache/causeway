package org.apache.isis.testing.fakedata.integtests;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.webspringboot.IsisModuleCoreWebSpringBoot;
import org.apache.isis.persistence.jdo.datanucleus5.IsisModuleJdoDataNucleus5;
import org.apache.isis.security.bypass.IsisModuleSecurityBypass;
import org.apache.isis.testing.fakedata.fixtures.IsisModuleTstFakeDataFixtures;
import org.apache.isis.testing.fixtures.applib.IsisIntegrationTestAbstractWithFixtures;
import org.apache.isis.testing.fixtures.applib.IsisModuleTstFixturesApplib;

@SpringBootTest(
        classes = FakeDataModuleIntegTestAbstract.AppManifest.class
)
@TestPropertySource(IsisPresets.UseLog4j2Test)
@ContextConfiguration
@Transactional
public abstract class FakeDataModuleIntegTestAbstract extends IsisIntegrationTestAbstractWithFixtures {

        @Configuration
        @PropertySources({
                @PropertySource(IsisPresets.H2InMemory_withUniqueSchema),
                @PropertySource(IsisPresets.NoTranslations),
                @PropertySource(IsisPresets.DataNucleusAutoCreate),
        })
        @Import({
                IsisModuleCoreWebSpringBoot.class,
                IsisModuleSecurityBypass.class,
                IsisModuleJdoDataNucleus5.class,
                IsisModuleTstFixturesApplib.class,
                IsisModuleTstFakeDataFixtures.class
        })
        public static class AppManifest {
        }

}
