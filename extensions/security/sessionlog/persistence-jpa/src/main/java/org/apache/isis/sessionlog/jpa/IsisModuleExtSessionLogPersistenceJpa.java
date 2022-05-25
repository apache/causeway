package org.apache.isis.sessionlog.jpa;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.sessionlog.applib.IsisModuleExtSessionLogApplib;
import org.apache.isis.sessionlog.jpa.dom.SessionLogEntry;
import org.apache.isis.sessionlog.jpa.dom.SessionLogEntryRepository;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fixtures.applib.modules.ModuleWithFixtures;
import org.apache.isis.testing.fixtures.applib.teardown.jdo.TeardownFixtureJdoAbstract;


@Configuration
@Import({
    IsisModuleExtSessionLogApplib.class,

    SessionLogEntryRepository.class
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
