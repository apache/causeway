/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.core.runtime.services.publish;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.PublishedAction;
import org.apache.isis.applib.annotation.PublishedObject;
import org.apache.isis.applib.annotation.PublishedObject.ChangeKind;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactn.InteractionContext;
import org.apache.isis.applib.services.metrics.MetricsService;
import org.apache.isis.applib.services.publish.EventMetadata;
import org.apache.isis.applib.services.publish.EventPayload;
import org.apache.isis.applib.services.publish.EventType;
import org.apache.isis.applib.services.publish.ObjectStringifier;
import org.apache.isis.applib.services.publish.PublishedObjects;
import org.apache.isis.applib.services.publish.PublisherService;
import org.apache.isis.applib.services.publish.PublishingService;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.CommandUtil;
import org.apache.isis.core.metamodel.facets.actions.publish.PublishedActionFacet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.object.publishedobject.PublishedObjectFacet;
import org.apache.isis.core.metamodel.services.ixn.InteractionDtoServiceInternal;
import org.apache.isis.core.metamodel.services.publishing.PublishingServiceInternal;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtime.services.changes.ChangedObjectsServiceInternal;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;

/**
 * Wrapper around {@link PublishingService}.  Is a no-op if there is no injected service.
 */
@DomainService(nature = NatureOfService.DOMAIN)
@RequestScoped
public class PublishingServiceInternalDefault implements PublishingServiceInternal {

    private final static OidMarshaller OID_MARSHALLER = OidMarshaller.INSTANCE;

    //region > static helper functions
    private Function<ObjectAdapter, ObjectAdapter> notDestroyedElseEmpty() {
        return new Function<ObjectAdapter, ObjectAdapter>() {
            public ObjectAdapter apply(ObjectAdapter adapter) {
                if (adapter == null) {
                    return null;
                }
                if (!adapter.isDestroyed()) {
                    return adapter;
                }
                // objectstores such as JDO prevent the underlying pojo from being touched once it has been deleted.
                // we therefore replace that pojo with an 'empty' one.

                Object replacementObject = getPersistenceSession()
                        .instantiateAndInjectServices(adapter.getSpecification());
                getPersistenceSession().remapRecreatedPojo(adapter, replacementObject);
                return adapter;
            }
        };
    }
    //endregion

    //region > publishObjects
    @Override
    @Programmatic
    public void publishObjects() {

        if(suppress) {
            return;
        }

        // take a copy of enlisted adapters ... the JDO implementation of the PublishingService
        // creates further entities which would be enlisted; taking copy of the map avoids ConcurrentModificationException
        final Map<ObjectAdapter, ChangeKind> changeKindByEnlistedAdapter = Maps.newHashMap();
        changeKindByEnlistedAdapter.putAll(changedObjectsServiceInternal.getChangeKindByEnlistedAdapter());

        publishObjectsToPublishingService(changeKindByEnlistedAdapter);
        publishObjectsToPublisherServices(changeKindByEnlistedAdapter);
    }

    private void publishObjectsToPublishingService(final Map<ObjectAdapter, ChangeKind> changeKindByEnlistedAdapter) {

        if(publishingServiceIfAny == null) {
            return;
        }

        final String currentUser = userService.getUser().getName();
        final Timestamp timestamp = clockService.nowAsJavaSqlTimestamp();
        final ObjectStringifier stringifier = objectStringifier();

        for (final Map.Entry<ObjectAdapter, ChangeKind> adapterAndChange : changeKindByEnlistedAdapter.entrySet()) {
            final ObjectAdapter enlistedAdapter = adapterAndChange.getKey();
            final ChangeKind changeKind = adapterAndChange.getValue();

            publishObjectToPublishingService(
                    enlistedAdapter, changeKind, currentUser, timestamp, stringifier);
        }
    }

    private void publishObjectToPublishingService(
            final ObjectAdapter enlistedAdapter,
            final ChangeKind changeKind,
            final String currentUser,
            final Timestamp timestamp,
            final ObjectStringifier stringifier) {

        final PublishedObjectFacet publishedObjectFacet =
                enlistedAdapter.getSpecification().getFacet(PublishedObjectFacet.class);
        if(publishedObjectFacet == null) {
            return;
        }

        final PublishedObject.PayloadFactory payloadFactory = publishedObjectFacet.value();

        final RootOid enlistedAdapterOid = (RootOid) enlistedAdapter.getOid();
        final String enlistedAdapterClass = CommandUtil.targetClassNameFor(enlistedAdapter);
        final Bookmark enlistedTarget = enlistedAdapterOid.asBookmark();

        final EventMetadata metadata = newEventMetadata(
                currentUser, timestamp, changeKind, enlistedAdapterClass, enlistedTarget);

        final Object pojo = ObjectAdapter.Util.unwrap(undeletedElseEmpty(enlistedAdapter));
        final EventPayload payload = payloadFactory.payloadFor(pojo, changeKind);

        payload.withStringifier(stringifier);
        publishingServiceIfAny.publish(metadata, payload);
    }

    private void publishObjectsToPublisherServices(
            final Map<ObjectAdapter, ChangeKind> changeKindByEnlistedAdapter) {

        final Map<ObjectAdapter, ChangeKind> changeKindByPublishedAdapter =
                Maps.filterKeys(
                        changeKindByEnlistedAdapter,
                        PublishedObjectFacet.Predicates.isPublished());

        if(changeKindByPublishedAdapter.isEmpty()) {
            return;
        }

        final int numberLoaded = metricsService.numberObjectsLoaded();
        final int numberObjectPropertiesModified = changedObjectsServiceInternal.numberObjectPropertiesModified();
        final PublishedObjects publishedObjects = newPublishedObjects(numberLoaded, numberObjectPropertiesModified,
                changeKindByPublishedAdapter);

        for (PublisherService publisherService : publisherServices) {
            publisherService.publish(publishedObjects);
        }
    }

    private PublishedObjects newPublishedObjects(
            final int numberLoaded,
            final int numberObjectPropertiesModified,
            final Map<ObjectAdapter, ChangeKind> changeKindByPublishedAdapter) {

        final Command command = commandContext.getCommand();
        final UUID transactionUuid = command.getTransactionId();

        final String userName = userService.getUser().getName();
        final Timestamp timestamp = clockService.nowAsJavaSqlTimestamp();

        final Interaction interaction = interactionContext.getInteraction();

        final int nextEventSequence = interaction.next(Interaction.Sequence.INTERACTION.id());

        return new PublishedObjectsDefault(transactionUuid, nextEventSequence, userName, timestamp, numberLoaded, numberObjectPropertiesModified, changeKindByPublishedAdapter);
    }

    //endregion

    //region > publishAction

    @Programmatic
    public void publishAction(
            final Interaction.Execution execution,
            final ObjectAction objectAction,
            final IdentifiedHolder identifiedHolder,
            final ObjectAdapter targetAdapter,
            final List<ObjectAdapter> parameterAdapters,
            final ObjectAdapter resultAdapter) {

        if(suppress) {
            return;
        }
        publishActionToPublishingService(
                objectAction, identifiedHolder, targetAdapter, parameterAdapters, resultAdapter
        );

        publishToPublisherServices(execution);
    }

    private void publishActionToPublishingService(
            final ObjectAction objectAction,
            final IdentifiedHolder identifiedHolder,
            final ObjectAdapter targetAdapter,
            final List<ObjectAdapter> parameterAdapters,
            final ObjectAdapter resultAdapter) {
        if(publishingServiceIfAny == null) {
            return;
        }
        final String currentUser = userService.getUser().getName();
        final Timestamp timestamp = clockService.nowAsJavaSqlTimestamp();

        final PublishedActionFacet publishedActionFacet =
                identifiedHolder.getFacet(PublishedActionFacet.class);
        if(publishedActionFacet == null) {
            return;
        }

        final RootOid adapterOid = (RootOid) targetAdapter.getOid();
        final String oidStr = OID_MARSHALLER.marshal(adapterOid);
        final Identifier actionIdentifier = objectAction.getIdentifier();
        final String title = oidStr + ": " + actionIdentifier.toNameParmsIdentityString();

        final String actionTargetClass = CommandUtil.targetClassNameFor(targetAdapter);
        final String actionTargetAction = CommandUtil.targetMemberNameFor(objectAction);
        final Bookmark actionTarget = CommandUtil.bookmarkFor(targetAdapter);
        final String actionMemberIdentifier = CommandUtil.memberIdentifierFor(objectAction);

        final List<String> parameterNames;
        final List<Class<?>> parameterTypes;
        final Class<?> returnType;

        if(identifiedHolder instanceof FacetedMethod) {
            // should always be the case

            final FacetedMethod facetedMethod = (FacetedMethod) identifiedHolder;
            returnType = facetedMethod.getType();

            final List<FacetedMethodParameter> parameters = facetedMethod.getParameters();
            parameterNames = immutableList(Iterables.transform(parameters, FacetedMethodParameter.Functions.GET_NAME));
            parameterTypes = immutableList(Iterables.transform(parameters, FacetedMethodParameter.Functions.GET_TYPE));
        } else {
            parameterNames = null;
            parameterTypes = null;
            returnType = null;
        }

        final Interaction interaction = interactionContext.getInteraction();

        final int nextEventSequence = interaction.next(Interaction.Sequence.PUBLISHED_EVENT.id());
        final UUID transactionId = interaction.getTransactionId();
        final EventMetadata metadata = new EventMetadata(
                transactionId, nextEventSequence, EventType.ACTION_INVOCATION, currentUser, timestamp, title,
                actionTargetClass, actionTargetAction, actionTarget, actionMemberIdentifier, parameterNames,
                parameterTypes, returnType);

        final PublishedAction.PayloadFactory payloadFactory = publishedActionFacet.value();

        final ObjectStringifier stringifier = objectStringifier();

        final EventPayload payload = payloadFactory.payloadFor(
                identifiedHolder.getIdentifier(),
                ObjectAdapter.Util.unwrap(undeletedElseEmpty(targetAdapter)),
                ObjectAdapter.Util.unwrap(undeletedElseEmpty(parameterAdapters)),
                ObjectAdapter.Util.unwrap(undeletedElseEmpty(resultAdapter)));
        payload.withStringifier(stringifier);
        publishingServiceIfAny.publish(metadata, payload);
    }

    private static <T> List<T> immutableList(final Iterable<T> iterable) {
        return Collections.unmodifiableList(Lists.newArrayList(iterable));
    }

    private ObjectStringifier objectStringifier() {
        return new ObjectStringifier() {
                @Override
                public String toString(Object object) {
                    if(object == null) {
                        return null;
                    }
                    final ObjectAdapter adapter = isisSessionFactory.getCurrentSession()
                            .getPersistenceSession().adapterFor(object);
                    Oid oid = adapter.getOid();
                    return oid != null? oid.enString(): encodedValueOf(adapter);
                }
                private String encodedValueOf(ObjectAdapter adapter) {
                    EncodableFacet facet = adapter.getSpecification().getFacet(EncodableFacet.class);
                    return facet != null? facet.toEncodedString(adapter): adapter.toString();
                }
                @Override
                public String classNameOf(Object object) {
                    final ObjectAdapter adapter = getPersistenceSession().adapterFor(object);
                    final String className = adapter.getSpecification().getFullIdentifier();
                    return className;
                }
            };
    }

    private List<ObjectAdapter> undeletedElseEmpty(List<ObjectAdapter> parameters) {
        return Lists.newArrayList(Iterables.transform(parameters, notDestroyedElseEmpty()));
    }

    private ObjectAdapter undeletedElseEmpty(ObjectAdapter adapter) {
        return notDestroyedElseEmpty().apply(adapter);
    }

    private EventMetadata newEventMetadata(
            final String currentUser,
            final Timestamp timestamp,
            final ChangeKind changeKind,
            final String enlistedAdapterClass,
            final Bookmark enlistedTarget) {
        final EventType eventType = PublishingServiceInternalDefault.eventTypeFor(changeKind);

        final Interaction interaction = interactionContext.getInteraction();

        final int nextEventSequence = interaction.next(Interaction.Sequence.PUBLISHED_EVENT.id());
        final UUID transactionId = interaction.getTransactionId();
        return new EventMetadata(
                transactionId, nextEventSequence, eventType, currentUser, timestamp, enlistedTarget.toString(),
                enlistedAdapterClass, null, enlistedTarget, null, null, null, null);
    }

    private static EventType eventTypeFor(ChangeKind changeKind) {
        if(changeKind == ChangeKind.UPDATE) {
            return EventType.OBJECT_UPDATED;
        }
        if(changeKind == ChangeKind.CREATE) {
            return EventType.OBJECT_CREATED;
        }
        if(changeKind == ChangeKind.DELETE) {
            return EventType.OBJECT_DELETED;
        }
        throw new IllegalArgumentException("unknown ChangeKind '" + changeKind + "'");
    }
    //endregion

    //region > publishProperty

    @Override
    public void publishProperty(
            final Interaction.Execution execution) {

        if(suppress) {
            return;
        }

        publishToPublisherServices(execution);
    }


    //endregion

    //region > helper: publishToPublisherServices

    private void publishToPublisherServices(final Interaction.Execution<?,?> execution) {

        if(publisherServices == null || publisherServices.isEmpty()) {
            return;
        }

        for (final PublisherService publisherService : publisherServices) {
            publisherService.publish(execution);
        }
    }

    //endregion

    //region > suppress

    // this service is request scoped
    boolean suppress;

    @Programmatic
    @Override
    public <T> T withPublishingSuppressed(final Block<T> block) {
        try {
            suppress = true;
            return block.exec();
        } finally {
            suppress = false;
        }
    }

    //endregion

    //region > injected services
    @javax.inject.Inject
    private List<PublisherService> publisherServices;

    @javax.inject.Inject
    private PublishingService publishingServiceIfAny;

    @javax.inject.Inject
    private ChangedObjectsServiceInternal changedObjectsServiceInternal;

    @javax.inject.Inject
    private InteractionDtoServiceInternal interactionDtoServiceInternal;

    @javax.inject.Inject
    private CommandContext commandContext;

    @javax.inject.Inject
    private InteractionContext interactionContext;

    @javax.inject.Inject
    private ClockService clockService;

    @javax.inject.Inject
    private UserService userService;

    @javax.inject.Inject
    private MetricsService metricsService;

    @javax.inject.Inject
    private IsisSessionFactory isisSessionFactory;

    private PersistenceSession getPersistenceSession() {
        return isisSessionFactory.getCurrentSession().getPersistenceSession();
    }
    //endregion

}
