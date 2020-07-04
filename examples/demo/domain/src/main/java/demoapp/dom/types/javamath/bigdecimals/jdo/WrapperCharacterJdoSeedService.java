package demoapp.dom.types.javamath.bigdecimals.jdo;

import java.util.stream.Stream;

import javax.inject.Inject;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.core.runtime.events.app.AppLifecycleEvent;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;

@Service
public class JavaMathBigDecimalJdoSeedService {

    @EventListener(AppLifecycleEvent.class)
    public void onAppLifecycleEvent(AppLifecycleEvent event) {

        if (event.getEventType() == AppLifecycleEvent.EventType.appPostMetamodel) {
            fixtureScripts.run(new JavaMathBigDecimalJdoEntityFixture());
        }
    }

    @Inject
    FixtureScripts fixtureScripts;

    static class JavaMathBigDecimalJdoEntityFixture extends FixtureScript {

        @Override
        protected void execute(ExecutionContext executionContext) {
            Stream.of('a', 'b', 'c')
                    .map(JavaMathBigDecimalJdo::new)
                    .forEach(repositoryService::persist);
        }

        @Inject
        RepositoryService repositoryService;

    }
}
