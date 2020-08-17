package demoapp.dom.annotDomain.Property.publishing.spiimpl;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.schema.ixn.v2.InteractionDto;

import demoapp.dom.annotDomain.Property.publishing.PropertyPublishingJdo;

@Action(
    semantics = SemanticsOf.IDEMPOTENT
    , associateWith = "interactionExecutions"
)
public class PropertyPublishingJdo_clearInteractionExecutions {

    private final PropertyPublishingJdo propertyPublishingJdo;

    public PropertyPublishingJdo_clearInteractionExecutions(PropertyPublishingJdo propertyPublishingJdo) {
        this.propertyPublishingJdo = propertyPublishingJdo;
    }

    public List<InteractionDto> act() {
        publisherServiceSpiForProperties.clear();
        return (List<InteractionDto>) propertyPublishingJdo;
    }

    @Inject
    private PublisherServiceSpiForProperties publisherServiceSpiForProperties;
}
