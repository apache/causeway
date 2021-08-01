package org.apache.isis.extensions.secman.jpa.permission.dom;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.test.context.ActiveProfiles;

import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.runtimeservices.IsisModuleCoreRuntimeServices;
import org.apache.isis.extensions.secman.applib.ApplicationPermissionRepositoryIntegTestAbstract;
import org.apache.isis.extensions.secman.applib.mmm.MmmModule;
import org.apache.isis.extensions.secman.jpa.IsisModuleExtSecmanPersistenceJpa;
import org.apache.isis.security.bypass.IsisModuleSecurityBypass;

@SpringBootTest(
        classes = ApplicationPermissionRepository_IntegTest.AppManifest.class
)
@ActiveProfiles("test")
class ApplicationPermissionRepository_IntegTest extends ApplicationPermissionRepositoryIntegTestAbstract {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({
            IsisModuleCoreRuntimeServices.class,
            IsisModuleSecurityBypass.class,
            IsisModuleExtSecmanPersistenceJpa.class,

            MmmModule.class,
    })
    @PropertySources({
            @PropertySource(IsisPresets.UseLog4j2Test),
    })
    public static class AppManifest {
    }


}