package demoapp.dom.annotDomain.Property.publishing.spiimpl;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.schema.ixn.v2.InteractionDto;

import lombok.val;

import demoapp.dom.annotDomain.Property.publishing.PropertyPublishingJdo;

//tag::class[]
@Collection
public class PropertyPublishingJdo_interactionExecutions {
    // ...
//end::class[]

    private final PropertyPublishingJdo propertyPublishingJdo;

    public PropertyPublishingJdo_interactionExecutions(PropertyPublishingJdo propertyPublishingJdo) {
        this.propertyPublishingJdo = propertyPublishingJdo;
    }

//tag::class[]
    public List<InteractionDto> coll() {
        val list = new LinkedList<InteractionDto>();
        publisherServiceSpiForProperties
                .streamInteractionDtos()
                .forEach(list::push);   // reverse order
        return list;
    }

    @Inject
    PublisherServiceSpiForProperties publisherServiceSpiForProperties;
}
//end::class[]
