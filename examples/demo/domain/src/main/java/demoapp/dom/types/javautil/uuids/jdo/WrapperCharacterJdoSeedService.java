package demoapp.dom.types.javautil.uuids.jdo;

import java.util.stream.Stream;

import javax.inject.Inject;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.core.runtime.events.app.AppLifecycleEvent;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;

@Service
public class JavaUtilUuidJdoSeedService {

    @EventListener(AppLifecycleEvent.class)
    public void onAppLifecycleEvent(AppLifecycleEvent event) {

        if (event.getEventType() == AppLifecycleEvent.EventType.appPostMetamodel) {
            fixtureScripts.run(new JavaUtilUuidJdoEntityFixture());
        }
    }

    @Inject
    FixtureScripts fixtureScripts;

    static class JavaUtilUuidJdoEntityFixture extends FixtureScript {

        @Override
        protected void execute(ExecutionContext executionContext) {
            Stream.of('a', 'b', 'c')
                    .map(JavaUtilUuidJdo::new)
                    .forEach(repositoryService::persist);
        }

        @Inject
        RepositoryService repositoryService;

    }
}
