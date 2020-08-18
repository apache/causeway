package demoapp.dom.annotDomain.Property.publishing.spiimpl;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.SemanticsOf;

import demoapp.dom.annotDomain.Property.publishing.PropertyPublishingJdo;

//tag::class[]
@Action(
    semantics = SemanticsOf.IDEMPOTENT
    , associateWith = "interactionExecutions"
)
public class PropertyPublishingJdo_clearInteractionExecutions {
    // ...
//end::class[]

    private final PropertyPublishingJdo propertyPublishingJdo;

    public PropertyPublishingJdo_clearInteractionExecutions(PropertyPublishingJdo propertyPublishingJdo) {
        this.propertyPublishingJdo = propertyPublishingJdo;
    }

//tag::class[]
    public PropertyPublishingJdo act() {
        publisherServiceSpiForProperties.clear();
        return propertyPublishingJdo;
    }

    @Inject
    PublisherServiceSpiForProperties publisherServiceSpiForProperties;
}
//end::class[]
