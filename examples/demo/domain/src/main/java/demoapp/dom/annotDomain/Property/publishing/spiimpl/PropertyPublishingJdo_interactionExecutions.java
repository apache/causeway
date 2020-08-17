package demoapp.dom.annotDomain.Property.publishing.spiimpl;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.schema.ixn.v2.InteractionDto;

import demoapp.dom.annotDomain.Property.publishing.PropertyPublishingJdo;

@Collection
public class PropertyPublishingJdo_interactionExecutions {

    private final PropertyPublishingJdo propertyPublishingJdo;

    public PropertyPublishingJdo_interactionExecutions(PropertyPublishingJdo propertyPublishingJdo) {
        this.propertyPublishingJdo = propertyPublishingJdo;
    }

    public List<InteractionDto> coll() {
        return publisherServiceSpiForProperties
                .streamInteractionDtos()
                .collect(Collectors.toList());
    }

    @Inject
    private PublisherServiceSpiForProperties publisherServiceSpiForProperties;
}
