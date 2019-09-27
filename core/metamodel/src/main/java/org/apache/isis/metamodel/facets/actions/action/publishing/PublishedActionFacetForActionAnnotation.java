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

package org.apache.isis.metamodel.facets.actions.action.publishing;

import java.util.List;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.config.IsisConfigurationLegacy;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.actions.publish.PublishedActionFacet;
import org.apache.isis.metamodel.facets.actions.publish.PublishedActionFacetAbstract;
import org.apache.isis.metamodel.facets.actions.semantics.ActionSemanticsFacet;

public class PublishedActionFacetForActionAnnotation extends PublishedActionFacetAbstract {

    public static PublishedActionFacet create(
            final List<Action> actions,
            final IsisConfigurationLegacy configuration,
            final FacetHolder holder) {

        final PublishActionsConfiguration setting = PublishActionsConfiguration.parse(configuration);

        return actions.stream()
                .map(Action::publishing)
                .filter(publishing -> publishing != Publishing.NOT_SPECIFIED)
                .findFirst()
                .map(publishing -> {
                    switch (publishing) {
                    case AS_CONFIGURED:

                        switch (setting) {
                        case NONE:
                            return null;
                        case IGNORE_SAFE:
                            if (hasSafeSemantics(holder)) {
                                return null;
                            }
                            // else fall through
                        default:
                            return (PublishedActionFacet)new PublishedActionFacetForActionAnnotationAsConfigured(holder);
                        }
                    case DISABLED:
                        return null;
                    case ENABLED:
                        return new PublishedActionFacetForActionAnnotation(holder);
                    default:
                    }
                    throw new IllegalStateException("publishing '" + publishing + "' not recognised");
                })
                .orElseGet(() -> {
                    switch (setting) {
                    case NONE:
                        return null;
                    case IGNORE_SAFE:
                        if (hasSafeSemantics(holder)) {
                            return null;
                        }
                        // else fall through
                    default:
                        return new PublishedActionFacetFromConfiguration(holder);
                    }
                });
    }

    private static boolean hasSafeSemantics(final FacetHolder holder) {
        final ActionSemanticsFacet actionSemanticsFacet = holder.getFacet(ActionSemanticsFacet.class);
        if(actionSemanticsFacet == null) {
            throw new IllegalStateException("Require ActionSemanticsFacet in order to process");
        }

        return actionSemanticsFacet.value().isSafeInNature();
    }

    PublishedActionFacetForActionAnnotation(
            final FacetHolder holder) {
        super(holder);
    }

}
