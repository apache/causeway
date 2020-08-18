package demoapp.dom.annotDomain.Action.publishing.spiimpl;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.schema.ixn.v2.InteractionDto;

import lombok.val;

import demoapp.dom.annotDomain.Action.publishing.ActionPublishingJdo;

//tag::class[]
@Collection
public class ActionPublishingJdo_interactionExecutions {
    // ...
//end::class[]

    private final ActionPublishingJdo actionPublishingJdo;

    public ActionPublishingJdo_interactionExecutions(ActionPublishingJdo actionPublishingJdo) {
        this.actionPublishingJdo = actionPublishingJdo;
    }

//tag::class[]
    public List<InteractionDto> coll() {
        val list = new LinkedList<InteractionDto>();
        publisherServiceSpiForActions
                .streamInteractionDtos()
                .forEach(list::push);   // reverse order
        return list;
    }

    @Inject
    private PublisherServiceSpiForActions publisherServiceSpiForActions;
}
//end::class[]
