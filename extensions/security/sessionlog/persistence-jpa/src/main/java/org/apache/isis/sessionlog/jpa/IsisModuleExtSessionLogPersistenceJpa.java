package org.apache.isis.sessionlog.jpa;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.persistence.jpa.eclipselink.IsisModulePersistenceJpaEclipselink;
import org.apache.isis.sessionlog.applib.IsisModuleExtSessionLogApplib;
import org.apache.isis.sessionlog.jpa.dom.SessionLogEntry;
import org.apache.isis.sessionlog.jpa.dom.SessionLogEntryRepository;
import org.apache.isis.testing.fixtures.applib.IsisModuleTestingFixturesApplib;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fixtures.applib.modules.ModuleWithFixtures;
import org.apache.isis.testing.fixtures.applib.teardown.jdo.TeardownFixtureJdoAbstract;


@Configuration
@Import({
        // modules
        IsisModuleTestingFixturesApplib.class,
        IsisModuleExtSessionLogApplib.class,
        IsisModulePersistenceJpaEclipselink.class,

        // services
        SessionLogEntryRepository.class,

        // entities, eager meta-model introspection
        SessionLogEntry.class,
})
@EntityScan(basePackageClasses = {
        SessionLogEntry.class,
})
public class IsisModuleExtSessionLogPersistenceJpa implements ModuleWithFixtures {

    @Override
    public FixtureScript getTeardownFixture() {
        return new TeardownFixtureJdoAbstract() {
            @Override
            protected void execute(final ExecutionContext executionContext) {
                deleteFrom(SessionLogEntry.class);
            }
        };
    }

}
