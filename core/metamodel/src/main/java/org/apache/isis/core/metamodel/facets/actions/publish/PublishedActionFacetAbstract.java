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

package org.apache.isis.core.metamodel.facets.actions.publish;

import java.util.List;
import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.PublishedAction;
import org.apache.isis.applib.annotation.PublishingPayloadFactoryForAction;
import org.apache.isis.applib.services.publish.EventPayload;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.SingleValueFacetAbstract;

public abstract class PublishedActionFacetAbstract extends SingleValueFacetAbstract<PublishedAction.PayloadFactory> implements PublishedActionFacet {

    public static Class<? extends Facet> type() {
        return PublishedActionFacet.class;
    }

    public PublishedActionFacetAbstract(final PublishingPayloadFactoryForAction payloadFactory, final FacetHolder holder) {
        this(legacyPayloadFactoryFor(payloadFactory), holder);
    }

    public PublishedActionFacetAbstract(final PublishedAction.PayloadFactory payloadFactory, final FacetHolder holder) {
        super(type(), payloadFactory, holder);
    }

    static PublishedAction.PayloadFactory legacyPayloadFactoryFor(final PublishingPayloadFactoryForAction publishingPayloadFactory) {
        if(publishingPayloadFactory == null) {
            return null;
        }
        if(publishingPayloadFactory instanceof PublishingPayloadFactoryForAction.Adapter) {
            final PublishingPayloadFactoryForAction.Adapter adapter = (PublishingPayloadFactoryForAction.Adapter) publishingPayloadFactory;
            return adapter.getPayloadFactory();
        }
        return new LegacyAdapter(publishingPayloadFactory);
    }

    public static class LegacyAdapter implements PublishedAction.PayloadFactory {

        private final PublishingPayloadFactoryForAction payloadFactory;

        LegacyAdapter(final PublishingPayloadFactoryForAction payloadFactory) {
            this.payloadFactory = payloadFactory;
        }

        @Override
        public EventPayload payloadFor(final Identifier actionIdentifier, final Object target, final List<Object> arguments, final Object result) {
            return payloadFactory.payloadFor(actionIdentifier, target, arguments, result);
        }

        /**
         * For testing only.
         */
        public PublishingPayloadFactoryForAction getPayloadFactory() {
            return payloadFactory;
        }
    }

}
