package org.apache.isis.core.runtime.persistence.objectstore.transaction;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.PublishedAction;
import org.apache.isis.applib.annotation.PublishedObject;
import org.apache.isis.applib.annotation.PublishedObject.ChangeKind;
import org.apache.isis.applib.services.publish.EventMetadata;
import org.apache.isis.applib.services.publish.EventPayload;
import org.apache.isis.applib.services.publish.ObjectStringifier;
import org.apache.isis.applib.services.publish.PublishingService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.actions.invoke.ActionInvocationFacet.CurrentInvocation;
import org.apache.isis.core.metamodel.spec.ObjectAdapterUtils;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;

/**
 * Wrapper around {@link PublishingService} that also includes the
 * {@link PublishedObject.PayloadFactory event} {@link PublishedAction.PayloadFactory canonicalizers}. 
 */
public class PublishingServiceWithDefaultPayloadFactories {

    private final PublishingService publishingService;
    private final PublishedObject.PayloadFactory defaultObjectPayloadFactory;
    private final PublishedAction.PayloadFactory defaultActionPayloadFactory;

    private final static Function<ObjectAdapter, ObjectAdapter> NOT_DESTROYED_ELSE_EMPTY = new Function<ObjectAdapter, ObjectAdapter>() {
        public ObjectAdapter apply(ObjectAdapter adapter) {
            if (!adapter.isDestroyed()) {
                return adapter;
            }
            // objectstores such as JDO prevent the underlying pojo from being touched once it has been deleted.
            // we therefore replace that pojo with an 'empty' one.
            Object replacementObject = adapter.getSpecification().createObject();
            getPersistenceSession().remapRecreatedPojo(adapter, replacementObject);
            return adapter;
        }
        protected PersistenceSession getPersistenceSession() {
            return IsisContext.getPersistenceSession();
        }

    };
    
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
            final ChangeKind changeKind, 
            final ObjectStringifier stringifier) {
        final PublishedObject.PayloadFactory payloadFactoryToUse = 
                payloadFactoryIfAny != null
                ? payloadFactoryIfAny
                : defaultObjectPayloadFactory;
        final EventPayload payload = payloadFactoryToUse.payloadFor(
                ObjectAdapterUtils.unwrapObject(undeletedElseEmpty(changedAdapter)), changeKind);
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
        ObjectAdapter target = currentInvocation.getTarget();
        ObjectAdapter result = currentInvocation.getResult();
        List<ObjectAdapter> parameters = currentInvocation.getParameters();
        final EventPayload payload = payloadFactoryToUse.payloadFor(
                currentInvocation.getAction().getIdentifier(),
                ObjectAdapterUtils.unwrapObject(undeletedElseEmpty(target)), 
                ObjectAdapterUtils.unwrapObjects(undeletedElseEmpty(parameters)), 
                ObjectAdapterUtils.unwrapObject(undeletedElseEmpty(result)));
        payload.withStringifier(stringifier);
        publishingService.publish(metadata, payload);
    }

    private static List<ObjectAdapter> undeletedElseEmpty(List<ObjectAdapter> parameters) {
        return Lists.newArrayList(Iterables.transform(parameters, NOT_DESTROYED_ELSE_EMPTY));
    }

    private static ObjectAdapter undeletedElseEmpty(ObjectAdapter adapter) {
        return NOT_DESTROYED_ELSE_EMPTY.apply(adapter);
    }
}
