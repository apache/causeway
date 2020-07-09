package demoapp.dom._infra.seed;

import java.util.function.Supplier;

import javax.inject.Inject;

import org.springframework.context.event.EventListener;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.core.runtime.events.app.AppLifecycleEvent;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;
import org.apache.isis.valuetypes.asciidoc.applib.value.AsciiDoc;

import demoapp.dom._infra.seed.SeedService;
import demoapp.dom.types.Samples;

public abstract class SeedServiceAbstract implements SeedService {

    private final Supplier<FixtureScript> fixtureScriptSupplier;

    protected SeedServiceAbstract(Supplier<FixtureScript> fixtureScriptSupplier) {
        this.fixtureScriptSupplier = fixtureScriptSupplier;
    }

    @EventListener(AppLifecycleEvent.class)
    public void onAppLifecycleEvent(AppLifecycleEvent event) {
        if (event.getEventType() == AppLifecycleEvent.EventType.appPostMetamodel) {
            fixtureScripts.run(fixtureScriptSupplier.get());
        }
    }

    @Override
    public void seed(FixtureScript parentFixtureScript, FixtureScript.ExecutionContext executionContext) {
        executionContext.executeChild(parentFixtureScript, this.fixtureScriptSupplier.get());
    }

    @Inject
    FixtureScripts fixtureScripts;

}
