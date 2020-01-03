package org.apache.isis.extensions.fakedata.integtests;


import org.apache.isis.config.IsisPresets;
import org.apache.isis.extensions.fixtures.IsisExtFixturesModule;
import org.apache.isis.extensions.fixtures.IsisIntegrationTestAbstractWithFixtures;
import org.apache.isis.jdo.IsisBootDataNucleus;
import org.apache.isis.runtime.spring.IsisBoot;
import org.apache.isis.security.bypass.IsisBootSecurityBypass;
import org.isisaddons.module.fakedata.fixture.FakeDataFixturesModule;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

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
                IsisBoot.class,
                IsisBootSecurityBypass.class,
                IsisBootDataNucleus.class,
                IsisExtFixturesModule.class,
                FakeDataFixturesModule.class
        })
        public static class AppManifest {
        }

}
