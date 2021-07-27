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
import org.apache.isis.core.transaction.changetracking.PropertyChangeTracker;

/**
 * @since 2.0 {@index}
 */
@Service
@Named("isis.transaction.PropertyChangePublisherJpa")
@Priority(PriorityPrecedence.EARLY)
@Qualifier("jpa")
//@Log4j2
public class PropertyChangePublisherJpa
extends PersistenceCallbackHandlerAbstract
implements
    MetricsService,
    PropertyChangeTracker {

    private final EntityPropertyChangePublisher entityPropertyChangePublisher;

    @Inject
    public PropertyChangePublisherJpa(
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
                PropertyChangeTracker
                .publishingPayloadForUpdate(entity, changeRecords));

    }

    @Override
    public void onPreRemove(final ManagedObject entity) {
        CallbackFacet.callCallback(entity, RemovingCallbackFacet.class);
        postLifecycleEventIfRequired(entity, RemovingLifecycleEventFacet.class);

        entityPropertyChangePublisher.publishChangedProperties(
                PropertyChangeTracker
                .publishingPayloadForDeletion(entity));
    }

    @Override
    public void onPostPersist(final ManagedObject entity) {
        CallbackFacet.callCallback(entity, PersistedCallbackFacet.class);
        postLifecycleEventIfRequired(entity, PersistedLifecycleEventFacet.class);

        entityPropertyChangePublisher.publishChangedProperties(
                PropertyChangeTracker
                .publishingPayloadForCreation(entity));
    }

    @Override
    public void onPostUpdate(final ManagedObject entity) {
        CallbackFacet.callCallback(entity, UpdatedCallbackFacet.class);
        postLifecycleEventIfRequired(entity, UpdatedLifecycleEventFacet.class);
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
