package org.apache.isis.core.runtime.system.transaction;

import java.util.UUID;

import org.apache.isis.applib.annotation.PublishedAction;
import org.apache.isis.applib.annotation.PublishedObject;
import org.apache.isis.applib.annotation.PublishedObject.EventCanonicalizer;
import org.apache.isis.applib.services.publish.CanonicalEvent;
import org.apache.isis.applib.services.publish.PublishingService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;

/**
 * Wrapper around {@link PublishingService} that also includes the
 * {@link PublishedObject.EventCanonicalizer event} {@link PublishedAction.EventCanonicalizer canonicalizers}. 
 * 
 * <p>
 * Acts as an internal contract between {@link IsisTransactionManager} and {@link IsisTransaction}.
 */
public class PublishingServiceWithCanonicalizers {

    private final PublishingService publishingService;
    private final PublishedObject.EventCanonicalizer defaultObjectEventCanonicalizer;
    private final PublishedAction.EventCanonicalizer defaultActionEventCanonicalizer;
    
    public PublishingServiceWithCanonicalizers(PublishingService publishingService, EventCanonicalizer defaultObjectEventCanonicalizer, PublishedAction.EventCanonicalizer defaultActionEventCanonicalizer) {
        this.publishingService = publishingService;
        this.defaultObjectEventCanonicalizer = defaultObjectEventCanonicalizer;
        this.defaultActionEventCanonicalizer = defaultActionEventCanonicalizer;
    }

    public void publishObject(PublishedObject.EventCanonicalizer eventCanonicalizer, UUID guid, String currentUser, long currentTimestampEpoch, ObjectAdapter changedAdapter) {
        PublishedObject.EventCanonicalizer eventCanonicalizerToUse = eventCanonicalizer != null? eventCanonicalizer: defaultObjectEventCanonicalizer;
        CanonicalEvent canonicalizedEvent = eventCanonicalizerToUse.canonicalizeObject(changedAdapter.getObject());
        
        publishingService.publish(guid, currentUser, currentTimestampEpoch, canonicalizedEvent);
    }

    
}
