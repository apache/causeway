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

package org.apache.isis.core.metamodel.facets.actions.action.publishing;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.annotation.PublishingPayloadFactoryForAction;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.actions.publish.PublishedActionFacet;
import org.apache.isis.core.metamodel.facets.actions.publish.PublishedActionFacetAbstract;
import org.apache.isis.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;

public class PublishedActionFacetForActionAnnotation extends PublishedActionFacetAbstract {

    public static PublishedActionFacet create(
            final Action action,
            final IsisConfiguration configuration,
            final FacetHolder holder) {

        final Publishing publishing = action != null ? action.publishing() : Publishing.AS_CONFIGURED;

        switch (publishing) {
            case AS_CONFIGURED:

                final PublishActionsConfiguration setting = PublishActionsConfiguration.parse(configuration);
                switch (setting) {
                    case NONE:
                        return null;
                    case IGNORE_SAFE:
                        final ActionSemanticsFacet actionSemanticsFacet = holder.getFacet(ActionSemanticsFacet.class);
                        if(actionSemanticsFacet == null) {
                            throw new IllegalStateException("Require ActionSemanticsFacet in order to process");
                        }

                        if(actionSemanticsFacet.value() == ActionSemantics.Of.SAFE) {
                            return  null;
                        }
                        // else fall through
                    default:
                        final PublishingPayloadFactoryForAction publishingPayloadFactory = newPayloadFactory(action);
                        return action != null
                                ? new PublishedActionFacetForActionAnnotation(publishingPayloadFactory, holder)
                                : new PublishedActionFacetFromConfiguration(publishingPayloadFactory, holder);
                }
            case DISABLED:
                return null;
            case ENABLED:
                return new PublishedActionFacetForActionAnnotation(
                        newPayloadFactory(action), holder);
        }
        return null;
    }

    /**
     * @return null means that the default payload factories will be used; this is handled within IsisTransaction.
     */
    private static PublishingPayloadFactoryForAction newPayloadFactory(final Action action) {
        if(action == null) {
            return null;
        }
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
        super(publishingPayloadFactory, holder);
    }

}
