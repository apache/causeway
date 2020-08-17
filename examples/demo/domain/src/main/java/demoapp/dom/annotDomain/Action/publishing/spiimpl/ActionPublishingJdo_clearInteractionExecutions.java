package demoapp.dom.annotDomain.Action.publishing.spiimpl;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.schema.ixn.v2.InteractionDto;

import demoapp.dom.annotDomain.Action.publishing.ActionPublishingJdo;

@Action(
    semantics = SemanticsOf.IDEMPOTENT
    , associateWith = "interactionExecutions"
)
public class ActionPublishingJdo_clearInteractionExecutions {

    private final ActionPublishingJdo actionPublishingJdo;

    public ActionPublishingJdo_clearInteractionExecutions(ActionPublishingJdo actionPublishingJdo) {
        this.actionPublishingJdo = actionPublishingJdo;
    }

    public List<InteractionDto> act() {
        publisherServiceSpiForActions.clear();
        return (List<InteractionDto>) actionPublishingJdo;
    }

    @Inject
    private PublisherServiceSpiForActions publisherServiceSpiForActions;
}
