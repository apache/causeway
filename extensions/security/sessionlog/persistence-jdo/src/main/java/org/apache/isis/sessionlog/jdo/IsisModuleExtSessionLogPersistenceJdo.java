package org.apache.isis.sessionlog.jdo;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.persistence.jdo.datanucleus.IsisModulePersistenceJdoDatanucleus;
import org.apache.isis.sessionlog.applib.IsisModuleExtSessionLogApplib;
import org.apache.isis.sessionlog.jdo.dom.SessionLogEntry;
import org.apache.isis.sessionlog.jdo.dom.SessionLogEntryRepository;
import org.apache.isis.testing.fixtures.applib.IsisModuleTestingFixturesApplib;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fixtures.applib.modules.ModuleWithFixtures;
import org.apache.isis.testing.fixtures.applib.teardown.jdo.TeardownFixtureJdoAbstract;


@Configuration
@Import({
        // modules
        IsisModuleTestingFixturesApplib.class,
        IsisModuleExtSessionLogApplib.class,
        IsisModulePersistenceJdoDatanucleus.class,

        // services
        SessionLogEntryRepository.class,

        // entities, eager meta-model introspection
        SessionLogEntry.class,
})
public class IsisModuleExtSessionLogPersistenceJdo implements ModuleWithFixtures {

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
