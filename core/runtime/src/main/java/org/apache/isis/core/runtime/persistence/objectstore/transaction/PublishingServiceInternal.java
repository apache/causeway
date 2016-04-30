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

package org.apache.isis.core.runtime.persistence.objectstore.transaction;

import java.util.List;

import javax.inject.Inject;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.PublishedAction;
import org.apache.isis.applib.annotation.PublishedObject;
import org.apache.isis.applib.annotation.PublishedObject.ChangeKind;
import org.apache.isis.applib.services.publish.EventMetadata;
import org.apache.isis.applib.services.publish.EventPayload;
import org.apache.isis.applib.services.publish.ObjectStringifier;
import org.apache.isis.applib.services.publish.PublishingService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet.CurrentInvocation;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;

/**
 * Wrapper around {@link PublishingService}.  Is a no-op if there is no injected service.
 */
@DomainService(nature = NatureOfService.DOMAIN)
public class PublishingServiceInternal {

    @Inject
    private PublishingService publishingServiceIfAny;

    private final static Function<ObjectAdapter, ObjectAdapter> NOT_DESTROYED_ELSE_EMPTY = new Function<ObjectAdapter, ObjectAdapter>() {
        public ObjectAdapter apply(ObjectAdapter adapter) {
            if(adapter == null) {
                return null;
            }
            if (!adapter.isDestroyed()) {
                return adapter;
            }
            // objectstores such as JDO prevent the underlying pojo from being touched once it has been deleted.
            // we therefore replace that pojo with an 'empty' one.

            Object replacementObject = getPersistenceSession().instantiateAndInjectServices(adapter.getSpecification());
            getPersistenceSession().remapRecreatedPojo(adapter, replacementObject);
            return adapter;
        }
        protected PersistenceSession getPersistenceSession() {
            return IsisContext.getPersistenceSession();
        }

    };


    @Programmatic
    public boolean canPublish() {
        return publishingServiceIfAny != null;
    }

    @Programmatic
    public void publishObject(
            final PublishedObject.PayloadFactory payloadFactory,
            final EventMetadata metadata, 
            final ObjectAdapter changedAdapter, 
            final ChangeKind changeKind, 
            final ObjectStringifier stringifier) {

        if (publishingServiceIfAny == null) {
            return;
        }

        final EventPayload payload = payloadFactory.payloadFor(
                ObjectAdapter.Util.unwrap(undeletedElseEmpty(changedAdapter)), changeKind);
        payload.withStringifier(stringifier);
        publishingServiceIfAny.publish(metadata, payload);
    }

    @Programmatic
    public void publishAction(
            final PublishedAction.PayloadFactory payloadFactory,
            final EventMetadata metadata, 
            final CurrentInvocation currentInvocation, 
            final ObjectStringifier stringifier) {

        if (publishingServiceIfAny == null) {
            return;
        }

        final ObjectAdapter target = currentInvocation.getTarget();
        final ObjectAdapter result = currentInvocation.getResult();
        final List<ObjectAdapter> parameters = currentInvocation.getParameters();
        final EventPayload payload = payloadFactory.payloadFor(
                currentInvocation.getIdentifiedHolder().getIdentifier(),
                ObjectAdapter.Util.unwrap(undeletedElseEmpty(target)), 
                ObjectAdapter.Util.unwrap(undeletedElseEmpty(parameters)), 
                ObjectAdapter.Util.unwrap(undeletedElseEmpty(result)));
        payload.withStringifier(stringifier);
        publishingServiceIfAny.publish(metadata, payload);
    }

    private static List<ObjectAdapter> undeletedElseEmpty(List<ObjectAdapter> parameters) {
        return Lists.newArrayList(Iterables.transform(parameters, NOT_DESTROYED_ELSE_EMPTY));
    }

    private static ObjectAdapter undeletedElseEmpty(ObjectAdapter adapter) {
        return NOT_DESTROYED_ELSE_EMPTY.apply(adapter);
    }
}
