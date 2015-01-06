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

package org.apache.isis.core.metamodel.facets.actions.action;

import java.util.List;
import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.PublishedAction;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.annotation.PublishingPayloadFactoryForAction;
import org.apache.isis.applib.services.publish.EventPayload;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.actions.command.CommandFacet;
import org.apache.isis.core.metamodel.facets.actions.publish.PublishedActionFacet;
import org.apache.isis.core.metamodel.facets.actions.publish.PublishedActionFacetAbstract;
import org.apache.isis.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;

public class PublishedActionFacetForActionAnnotation extends PublishedActionFacetAbstract {

    public static PublishedActionFacet create(
            final Action action,
            final IsisConfiguration configuration,
            final FacetHolder holder) {

        if (action == null) {
            return null;
        }

        final ActionSemanticsFacet actionSemanticsFacet = holder.getFacet(ActionSemanticsFacet.class);
        if(actionSemanticsFacet == null) {
            throw new IllegalStateException("Require ActionSemanticsFacet in order to process");
        }
        if(holder.containsDoOpFacet(CommandFacet.class)) {
            // do not replace
            return null;
        }

        final Publishing publishing = action.publishing();

        switch (publishing) {
            case AS_CONFIGURED:

                final PublishActionsConfiguration setting = PublishActionsConfiguration.parse(configuration);
                switch (setting) {
                    case NONE:
                        return null;
                    case IGNORE_SAFE:
                        if(actionSemanticsFacet.value() == ActionSemantics.Of.SAFE) {
                            return  null;
                        }
                        // else fall through
                    default:
                        return new PublishedActionFacetForActionAnnotation(
                                newPayloadFactory(action), holder);
                }
            case DISABLED:
                return null;
            case ENABLED:
                return new PublishedActionFacetForActionAnnotation(
                        newPayloadFactory(action), holder);
        }
        return null;
    }

    private static PublishingPayloadFactoryForAction newPayloadFactory(final Action action) {
        final Class<? extends PublishingPayloadFactoryForAction> payloadFactoryClass = action.publishingPayloadFactory();
        if(payloadFactoryClass == null) {
            return null;
        }

        try {
            return payloadFactoryClass.newInstance();
        } catch (final InstantiationException e) {
            return null;
        } catch (final IllegalAccessException e) {
            return null;
        }
    }

    public PublishedActionFacetForActionAnnotation(
            final PublishingPayloadFactoryForAction publishingPayloadFactory,
            final FacetHolder holder) {
        super(legacyPayloadFactoryFor(publishingPayloadFactory), holder);
    }

    private static PublishedAction.PayloadFactory legacyPayloadFactoryFor(PublishingPayloadFactoryForAction publishingPayloadFactory) {
        if(publishingPayloadFactory instanceof PublishingPayloadFactoryForAction.Adapter) {
            final PublishingPayloadFactoryForAction.Adapter adapter = (PublishingPayloadFactoryForAction.Adapter) publishingPayloadFactory;
            return adapter.getPayloadFactory();
        }
        return new LegacyAdapter(publishingPayloadFactory);
    }

    private static class LegacyAdapter implements PublishedAction.PayloadFactory {

        private final PublishingPayloadFactoryForAction payloadFactory;

        LegacyAdapter(final PublishingPayloadFactoryForAction payloadFactory) {
            this.payloadFactory = payloadFactory;
        }

        @Override
        public EventPayload payloadFor(final Identifier actionIdentifier, final Object target, final List<Object> arguments, final Object result) {
            return payloadFactory.payloadFor(actionIdentifier, target, arguments, result);
        }
    }


}
