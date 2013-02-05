package org.apache.isis.core.runtime.persistence.objectstore.transaction;

import java.util.List;
import java.util.UUID;

import org.apache.isis.applib.annotation.PublishedAction;
import org.apache.isis.applib.annotation.PublishedObject;
import org.apache.isis.applib.annotation.PublishedObject.EventCanonicalizer;
import org.apache.isis.applib.services.publish.CanonicalEvent;
import org.apache.isis.applib.services.publish.PublishingService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facets.actions.invoke.ActionInvocationFacet.CurrentInvocation;
import org.apache.isis.core.metamodel.spec.ObjectAdapterUtils;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

/**
 * Wrapper around {@link PublishingService} that also includes the
 * {@link PublishedObject.EventCanonicalizer event} {@link PublishedAction.EventCanonicalizer canonicalizers}. 
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
        final PublishedObject.EventCanonicalizer eventCanonicalizerToUse = eventCanonicalizer != null? eventCanonicalizer: defaultObjectEventCanonicalizer;
        final CanonicalEvent canonicalizedEvent = eventCanonicalizerToUse.canonicalizeObject(ObjectAdapterUtils.unwrapObject(changedAdapter));
        
        publishingService.publish(guid, currentUser, currentTimestampEpoch, canonicalizedEvent);
    }

    public void publishAction(PublishedAction.EventCanonicalizer value, UUID guid, String currentUser, long currentTimestampEpoch, CurrentInvocation currentInvocation) {
        publishAction(value, guid, currentUser, currentTimestampEpoch, currentInvocation.getTarget(), currentInvocation.getAction(), currentInvocation.getParameters(), currentInvocation.getResult());
    }

    private void publishAction(PublishedAction.EventCanonicalizer eventCanonicalizer, UUID guid, String currentUser, long currentTimestampEpoch, ObjectAdapter targetAdapter, IdentifiedHolder action, List<ObjectAdapter> parameterAdapters, ObjectAdapter resultAdapter) {
        final PublishedAction.EventCanonicalizer eventCanonicalizerToUse = eventCanonicalizer != null? eventCanonicalizer: defaultActionEventCanonicalizer;
        final CanonicalEvent canonicalizedEvent = eventCanonicalizerToUse.canonicalizeAction(
                ObjectAdapterUtils.unwrapObject(targetAdapter), 
                action.getIdentifier(), 
                ObjectAdapterUtils.unwrapObjects(parameterAdapters), 
                ObjectAdapterUtils.unwrapObject(resultAdapter));
        
        publishingService.publish(guid, currentUser, currentTimestampEpoch, canonicalizedEvent);
    }

    
}
