package demoapp.dom.annotDomain.Action.publishing.spiimpl;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.schema.ixn.v2.InteractionDto;

import demoapp.dom.annotDomain.Action.publishing.ActionPublishingJdo;

@Collection
public class ActionPublishingJdo_interactionExecutions {

    private final ActionPublishingJdo actionPublishingJdo;

    public ActionPublishingJdo_interactionExecutions(ActionPublishingJdo actionPublishingJdo) {
        this.actionPublishingJdo = actionPublishingJdo;
    }

    public List<InteractionDto> coll() {
        return publisherServiceSpiForActions
                .streamInteractionDtos()
                .collect(Collectors.toList());
    }

    @Inject
    private PublisherServiceSpiForActions publisherServiceSpiForActions;
}
