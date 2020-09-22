package demoapp.web.linebreaker;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.core.runtime.iactn.IsisInteractionTracker;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * 
 * REST endpoint to allow for remote application shutdown 
 *
 */
@DomainService(nature = NatureOfService.REST, objectType = "demo.LineBreaker")
@Log4j2
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class LineBreaker {

    final IsisInteractionTracker isisInteractionTracker;

    @Action(semantics = SemanticsOf.SAFE)
    public void shutdown() {
        log.info("about to shutdown the JVM");

        // allow for current interaction to complete gracefully
        isisInteractionTracker.currentInteraction()
        .ifPresent(interaction->{
            interaction.setOnClose(()->System.exit(0));
        });
    }


}
