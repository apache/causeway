package org.apache.isis.persistence.jpa.integration.changetracking;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.applib.services.metrics.MetricsService;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.facets.object.callbacks.CallbackFacet;
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
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.transaction.changetracking.EntityPropertyChangePublisher;
import org.apache.isis.core.transaction.changetracking.PersistenceCallbackHandlerAbstract;
import org.apache.isis.core.transaction.changetracking.PropertyChangeRecord;
import org.apache.isis.core.transaction.changetracking.PersistenceLifecycleTracker;

/**
 * @since 2.0 {@index}
 */
@Service
@Named("isis.transaction.PersistenceLifecycleEventPublisherJpa")
@Priority(PriorityPrecedence.EARLY)
@Qualifier("jpa")
//@Log4j2
public class PersistenceLifecycleEventPublisherJpa
extends PersistenceCallbackHandlerAbstract
implements
    MetricsService,
    PersistenceLifecycleTracker {

    private final EntityPropertyChangePublisher entityPropertyChangePublisher;

    @Inject
    public PersistenceLifecycleEventPublisherJpa(
            final EventBusService eventBusService,
            final EntityPropertyChangePublisher entityPropertyChangePublisher) {
        super(eventBusService);
        this.entityPropertyChangePublisher = entityPropertyChangePublisher;
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
                PersistenceLifecycleTracker
                .publishingPayloadForUpdate(entity, changeRecords));

    }

    @Override
    public void onPreRemove(final ManagedObject entity) {
        CallbackFacet.callCallback(entity, RemovingCallbackFacet.class);
        postLifecycleEventIfRequired(entity, RemovingLifecycleEventFacet.class);

        entityPropertyChangePublisher.publishChangedProperties(
                PersistenceLifecycleTracker
                .publishingPayloadForDeletion(entity));
    }

    @Override
    public void onPostPersist(final ManagedObject entity) {
        CallbackFacet.callCallback(entity, PersistedCallbackFacet.class);
        postLifecycleEventIfRequired(entity, PersistedLifecycleEventFacet.class);

        entityPropertyChangePublisher.publishChangedProperties(
                PersistenceLifecycleTracker
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

    // -- METRICS

    @Override
    public int numberEntitiesLoaded() {
        return -1; // n/a
    }

    @Override
    public int numberEntitiesDirtied() {
        return -1; // n/a
    }

}
