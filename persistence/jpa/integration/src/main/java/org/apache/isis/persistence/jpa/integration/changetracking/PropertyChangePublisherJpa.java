package org.apache.isis.persistence.jpa.integration.changetracking;

import java.sql.Timestamp;
import java.util.UUID;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.applib.services.metrics.MetricsService;
import org.apache.isis.applib.services.publishing.spi.EntityPropertyChange;
import org.apache.isis.applib.services.xactn.TransactionId;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.CommandUtil;
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
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.transaction.changetracking.EntityPropertyChangePublisher;
import org.apache.isis.core.transaction.changetracking.HasEnlistedEntityPropertyChanges;
import org.apache.isis.core.transaction.changetracking.PersistenceCallbackHandlerAbstract;
import org.apache.isis.core.transaction.changetracking.PreAndPostValue;
import org.apache.isis.core.transaction.changetracking.PropertyChangePublisher;
import org.apache.isis.core.transaction.changetracking.PropertyChangeRecord;
import org.apache.isis.core.transaction.changetracking.events.IsisTransactionPlaceholder;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * @since 2.0 {@index}
 */
@Service
@Named("isis.transaction.PropertyChangePublisherJpa")
@Priority(PriorityPrecedence.EARLY)
@Qualifier("jpa")
@Log4j2
public class PropertyChangePublisherJpa
extends PersistenceCallbackHandlerAbstract
implements
    MetricsService,
    PropertyChangePublisher {

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

        val payload = new HasEnlistedEntityPropertyChanges() {

            @Override
            public Can<EntityPropertyChange> getPropertyChanges(
                    final Timestamp timestamp,
                    final String user,
                    final TransactionId txId) {

                return changeRecords
                .map(changeRecord->{

                    log.debug("publish property change {} value '{}' -> '{}'",
                            entity.getSpecification().getLogicalTypeName(),
                            changeRecord.getPropertyId(),
                            changeRecord.getPreAndPostValue().getPre(),
                            changeRecord.getPreAndPostValue().getPost());

                    return toPropertyChange(changeRecord,
                            timestamp,
                            user,
                            txId);
                });

            }

        };

        CallbackFacet.callCallback(entity, UpdatingCallbackFacet.class);
        postLifecycleEventIfRequired(entity, UpdatingLifecycleEventFacet.class);

        entityPropertyChangePublisher.publishChangedProperties(payload);
    }

    @Override
    public void onPreRemove(final ManagedObject entity) {
        CallbackFacet.callCallback(entity, RemovingCallbackFacet.class);
        postLifecycleEventIfRequired(entity, RemovingLifecycleEventFacet.class);


        val payload = new HasEnlistedEntityPropertyChanges() {

            @Override
            public Can<EntityPropertyChange> getPropertyChanges(
                    final Timestamp timestamp,
                    final String user,
                    final TransactionId txId) {

                return entity
                .getSpecification()
                .streamProperties(MixedIn.EXCLUDED)
                .filter(property->!property.isMixedIn())
                .filter(property->!property.isNotPersisted())
                .map(property->PropertyChangeRecord.of(
                            entity,
                            property,
                            PreAndPostValue
                                .pre(property.get(entity))
                                .withPost(IsisTransactionPlaceholder.DELETED))
                )
                .map(changeRecord->{

                    log.debug("publish property change {} value '{}' -> '{}'",
                            entity.getSpecification().getLogicalTypeName(),
                            changeRecord.getPropertyId(),
                            changeRecord.getPreAndPostValue().getPre(),
                            changeRecord.getPreAndPostValue().getPost());

                    return toPropertyChange(changeRecord,
                            timestamp,
                            user,
                            txId);
                })
                .collect(Can.toCan());

            }

        };

        entityPropertyChangePublisher.publishChangedProperties(payload);
    }

    @Override
    public void onPostPersist(final ManagedObject entity) {
        CallbackFacet.callCallback(entity, PersistedCallbackFacet.class);
        postLifecycleEventIfRequired(entity, PersistedLifecycleEventFacet.class);

        val payload = new HasEnlistedEntityPropertyChanges() {

            @Override
            public Can<EntityPropertyChange> getPropertyChanges(
                    final Timestamp timestamp,
                    final String user,
                    final TransactionId txId) {

                return entity
                .getSpecification()
                .streamProperties(MixedIn.EXCLUDED)
                .filter(property->!property.isMixedIn())
                .filter(property->!property.isNotPersisted())
                .map(property->PropertyChangeRecord.of(
                            entity,
                            property,
                            PreAndPostValue
                                .pre(IsisTransactionPlaceholder.NEW)
                                .withPost(property.get(entity)))
                )
                .map(changeRecord->{

                    log.debug("publish property change {} value '{}' -> '{}'",
                            entity.getSpecification().getLogicalTypeName(),
                            changeRecord.getPropertyId(),
                            changeRecord.getPreAndPostValue().getPre(),
                            changeRecord.getPreAndPostValue().getPost());

                    return toPropertyChange(changeRecord,
                            timestamp,
                            user,
                            txId);
                })
                .collect(Can.toCan());

            }

        };

        entityPropertyChangePublisher.publishChangedProperties(payload);
    }

    @Override
    public void onPostUpdate(final ManagedObject entity) {
        CallbackFacet.callCallback(entity, UpdatedCallbackFacet.class);
        postLifecycleEventIfRequired(entity, UpdatedLifecycleEventFacet.class);
    }

    @Override
    public void onPostRemove(final ManagedObject entity) {
        // not used
    }

    @Override
    public int numberEntitiesLoaded() {
        return -1; // n/a
    }

    @Override
    public int numberEntitiesDirtied() {
        return -1; // n/a
    }

    // -- HELPER

    private EntityPropertyChange toPropertyChange(
            final PropertyChangeRecord record,
            final Timestamp timestamp,
            final String user,
            final TransactionId txId) {

        val entity = record.getEntity();
        val spec = entity.getSpecification();
        val property = record.getProperty();

        final Bookmark target = ManagedObjects.bookmarkElseFail(entity);
        final String propertyId = property.getId();
        final String memberId = property.getFeatureIdentifier().getFullIdentityString();
        final String preValueStr = record.getPreAndPostValue().getPreString();
        final String postValueStr = record.getPreAndPostValue().getPostString();
        final String targetClass = CommandUtil.targetClassNameFor(spec);

        final UUID transactionId = txId.getInteractionId();
        final int sequence = txId.getSequence();


        return EntityPropertyChange.of(
                transactionId, sequence, targetClass, target,
                memberId, propertyId, preValueStr, postValueStr, user, timestamp);
    }

}
