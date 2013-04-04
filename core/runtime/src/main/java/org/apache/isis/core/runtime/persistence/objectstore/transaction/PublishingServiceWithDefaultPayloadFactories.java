package org.apache.isis.core.runtime.persistence.objectstore.transaction;

import org.apache.isis.applib.annotation.PublishedAction;
import org.apache.isis.applib.annotation.PublishedObject;
import org.apache.isis.applib.services.publish.EventMetadata;
import org.apache.isis.applib.services.publish.EventPayload;
import org.apache.isis.applib.services.publish.ObjectStringifier;
import org.apache.isis.applib.services.publish.PublishingService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.actions.invoke.ActionInvocationFacet.CurrentInvocation;
import org.apache.isis.core.metamodel.spec.ObjectAdapterUtils;

/**
 * Wrapper around {@link PublishingService} that also includes the
 * {@link PublishedObject.PayloadFactory event} {@link PublishedAction.PayloadFactory canonicalizers}. 
 */
public class PublishingServiceWithDefaultPayloadFactories {

    private final PublishingService publishingService;
    private final PublishedObject.PayloadFactory defaultObjectPayloadFactory;
    private final PublishedAction.PayloadFactory defaultActionPayloadFactory;
    
    public PublishingServiceWithDefaultPayloadFactories (
            final PublishingService publishingService, 
            final PublishedObject.PayloadFactory defaultObjectPayloadFactory, 
            final PublishedAction.PayloadFactory defaultActionPayloadFactory) {
        this.publishingService = publishingService;
        this.defaultObjectPayloadFactory = defaultObjectPayloadFactory;
        this.defaultActionPayloadFactory = defaultActionPayloadFactory;
    }

    public void publishObject(
            final PublishedObject.PayloadFactory payloadFactoryIfAny, 
            final EventMetadata metadata, 
            final ObjectAdapter changedAdapter, 
            final ObjectStringifier stringifier) {
        final PublishedObject.PayloadFactory payloadFactoryToUse = 
                payloadFactoryIfAny != null
                ? payloadFactoryIfAny
                : defaultObjectPayloadFactory;
        final EventPayload payload = payloadFactoryToUse.payloadFor(ObjectAdapterUtils.unwrapObject(changedAdapter));
        payload.withStringifier(stringifier);
        publishingService.publish(metadata, payload);
    }

    public void publishAction(
            final PublishedAction.PayloadFactory payloadFactoryIfAny, 
            final EventMetadata metadata, 
            final CurrentInvocation currentInvocation, 
            final ObjectStringifier stringifier) {
        final PublishedAction.PayloadFactory payloadFactoryToUse = 
                payloadFactoryIfAny != null
                ? payloadFactoryIfAny
                : defaultActionPayloadFactory;
        final EventPayload payload = payloadFactoryToUse.payloadFor(
                currentInvocation.getAction().getIdentifier(),
                ObjectAdapterUtils.unwrapObject(currentInvocation.getTarget()), 
                ObjectAdapterUtils.unwrapObjects(currentInvocation.getParameters()), 
                ObjectAdapterUtils.unwrapObject(currentInvocation.getResult()));
        payload.withStringifier(stringifier);
        publishingService.publish(metadata, payload);
    }
}
