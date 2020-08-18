package demoapp.dom.annotDomain.Action.publishing.spiimpl;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.SemanticsOf;

import demoapp.dom.annotDomain.Action.publishing.ActionPublishingJdo;

//tag::class[]
@Action(
    semantics = SemanticsOf.IDEMPOTENT
    , associateWith = "interactionExecutions"
)
public class ActionPublishingJdo_clearInteractionExecutions {
    // ...
//end::class[]

    private final ActionPublishingJdo actionPublishingJdo;

    public ActionPublishingJdo_clearInteractionExecutions(ActionPublishingJdo actionPublishingJdo) {
        this.actionPublishingJdo = actionPublishingJdo;
    }

//tag::class[]
    public ActionPublishingJdo act() {
        publisherServiceSpiForActions.clear();
        return actionPublishingJdo;
    }

    @Inject
    PublisherServiceSpiForActions publisherServiceSpiForActions;
}
//end::class[]
