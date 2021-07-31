package org.apache.isis.core.runtimeservices.publish;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.facets.object.callbacks.CallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.CreatedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.CreatedLifecycleEventFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.LoadedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.LoadedLifecycleEventFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistedLifecycleEventFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistingCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistingLifecycleEventFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.RemovingCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.RemovingLifecycleEventFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.UpdatedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.UpdatedLifecycleEventFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.UpdatingCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.UpdatingLifecycleEventFacet;
import org.apache.isis.core.metamodel.services.objectlifecycle.ObjectLifecyclePublisher;
import org.apache.isis.core.metamodel.services.objectlifecycle.PropertyChangeRecord;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.transaction.changetracking.EntityPropertyChangePublisher;
import org.apache.isis.core.transaction.changetracking.PersistenceCallbackHandlerAbstract;

/**
 * @see ObjectLifecyclePublisher
 * @since 2.0 {@index}
 */
@Service
@Named("isis.runtimeservices.ObjectLifecyclePublisherDefault")
@Priority(PriorityPrecedence.EARLY)
@Qualifier("Default")
//@Log4j2
public class ObjectLifecyclePublisherDefault
extends PersistenceCallbackHandlerAbstract
implements
    ObjectLifecyclePublisher {

    private final EntityPropertyChangePublisher entityPropertyChangePublisher;

    @Inject
    public ObjectLifecyclePublisherDefault(
            final EventBusService eventBusService,
            final EntityPropertyChangePublisher entityPropertyChangePublisher) {
        super(eventBusService);
        this.entityPropertyChangePublisher = entityPropertyChangePublisher;
    }

    @Override
    public void onPostCreate(final ManagedObject domainObject) {
        CallbackFacet.callCallback(domainObject, CreatedCallbackFacet.class);
        postLifecycleEventIfRequired(domainObject, CreatedLifecycleEventFacet.class);
    }

    @Override
    public void onPrePersist(final ManagedObject entity) {
        CallbackFacet.callCallback(entity, PersistingCallbackFacet.class);
        postLifecycleEventIfRequired(entity, PersistingLifecycleEventFacet.class);
    }

    @Override
    public void onPreUpdate(
            final ManagedObject entity,
            final Can<PropertyChangeRecord> changeRecords) {

        if(changeRecords.isEmpty()) {
            return;
        }

        CallbackFacet.callCallback(entity, UpdatingCallbackFacet.class);
        postLifecycleEventIfRequired(entity, UpdatingLifecycleEventFacet.class);

        entityPropertyChangePublisher.publishChangedProperties(
                ObjectLifecyclePublisher
                .publishingPayloadForUpdate(entity, changeRecords));

    }

    @Override
    public void onPreRemove(final ManagedObject entity) {
        CallbackFacet.callCallback(entity, RemovingCallbackFacet.class);
        postLifecycleEventIfRequired(entity, RemovingLifecycleEventFacet.class);

        entityPropertyChangePublisher.publishChangedProperties(
                ObjectLifecyclePublisher
                .publishingPayloadForDeletion(entity));
    }

    @Override
    public void onPostPersist(final ManagedObject entity) {
        CallbackFacet.callCallback(entity, PersistedCallbackFacet.class);
        postLifecycleEventIfRequired(entity, PersistedLifecycleEventFacet.class);

        entityPropertyChangePublisher.publishChangedProperties(
                ObjectLifecyclePublisher
                .publishingPayloadForCreation(entity));
    }

    @Override
    public void onPostUpdate(final ManagedObject entity) {
        CallbackFacet.callCallback(entity, UpdatedCallbackFacet.class);
        postLifecycleEventIfRequired(entity, UpdatedLifecycleEventFacet.class);
    }

    @Override
    public void onPostLoad(final ManagedObject entity) {
        CallbackFacet.callCallback(entity, LoadedCallbackFacet.class);
        postLifecycleEventIfRequired(entity, LoadedLifecycleEventFacet.class);
    }

}
