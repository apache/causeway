package org.apache.isis.subdomains.excel.integtests;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import org.apache.isis.config.IsisPresets;
import org.apache.isis.subdomains.excel.fixtures.IsisModuleSubExcelFixtures;
import org.apache.isis.extensions.fixtures.IsisExtFixturesModule;
import org.apache.isis.extensions.fixtures.IsisIntegrationTestAbstractWithFixtures;
import org.apache.isis.jdo.IsisBootDataNucleus;
import org.apache.isis.runtime.spring.IsisBoot;
import org.apache.isis.security.bypass.IsisBootSecurityBypass;

@SpringBootTest(
        classes = ExcelModuleIntegTestAbstract.AppManifest.class
)
@TestPropertySource(IsisPresets.UseLog4j2Test)
@ContextConfiguration
@Transactional
public abstract class ExcelModuleIntegTestAbstract extends IsisIntegrationTestAbstractWithFixtures {

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

            /*
                new ExcelFixturesModule(),
            FakeDataModule.class
             */
            IsisModuleSubExcelFixtures.class
    })
    public static class AppManifest {
    }

}
