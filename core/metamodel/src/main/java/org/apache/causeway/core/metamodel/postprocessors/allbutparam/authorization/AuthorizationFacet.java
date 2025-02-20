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
package org.apache.causeway.core.metamodel.postprocessors.allbutparam.authorization;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.interactions.ActionVisibilityContext;
import org.apache.causeway.core.metamodel.interactions.CollectionVisibilityContext;
import org.apache.causeway.core.metamodel.interactions.DisablingInteractionAdvisor;
import org.apache.causeway.core.metamodel.interactions.HidingInteractionAdvisor;
import org.apache.causeway.core.metamodel.interactions.PropertyVisibilityContext;
import org.apache.causeway.core.metamodel.interactions.VisibilityContext;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

import org.jspecify.annotations.NonNull;

/**
 * Optionally hide or disable an object, property, collection or action
 * depending on the authorization.
 */
public interface AuthorizationFacet
extends Facet, HidingInteractionAdvisor, DisablingInteractionAdvisor {

    public static boolean hidesProperty(
            final @NonNull OneToOneAssociation property,
            final @NonNull VisibilityContext vc) {

        return property.lookupFacet(AuthorizationFacet.class)
                .map(facet->facet.hides(
                        new PropertyVisibilityContext(
                                vc.head(),
                                property.getFeatureIdentifier(),
                                vc.initiatedBy(),
                                vc.where(),
                                vc.renderPolicy())) != null)
                .orElse(false);
    }

    public static boolean hidesCollection(
            final @NonNull OneToManyAssociation collection,
            final @NonNull VisibilityContext vc) {

        return collection.lookupFacet(AuthorizationFacet.class)
                .map(facet->facet.hides(
                        new CollectionVisibilityContext(
                                vc.head(),
                                collection.getFeatureIdentifier(),
                                vc.initiatedBy(),
                                vc.where(),
                                vc.renderPolicy())) != null)
                .orElse(false);
    }

    public static boolean hidesAction(
            final @NonNull ObjectAction action,
            final @NonNull VisibilityContext vc) {

        return action.lookupFacet(AuthorizationFacet.class)
                .map(facet->facet.hides(
                        new ActionVisibilityContext(
                                vc.head(),
                                action,
                                action.getFeatureIdentifier(),
                                vc.initiatedBy(),
                                vc.where(),
                                vc.renderPolicy())) != null)
                .orElse(false);
    }

    /**
     * @param identifier - presence results in a more detailed message (including feature origin)
     * @param mmc - if present AND when NOT PROTOTYPING AND when identifier is also present,
     *      results in a more concise message,
     *      only including the friendly member name (omitting the type's name)
     */
    public static String formatNotAuthorizedToEdit(
            final @Nullable Identifier identifier,
            final @Nullable MetaModelContext mmc) {
        var template = identifier==null
            ? ProgrammingModelConstants.MessageTemplate.NOT_AUTHORIZED_TO_EDIT_OR_USE
            : mmc!=null && !mmc.getSystemEnvironment().isPrototyping()
                ? ProgrammingModelConstants.MessageTemplate.NOT_AUTHORIZED_TO_EDIT_OR_USE_MEMBER
                : ProgrammingModelConstants.MessageTemplate.NOT_AUTHORIZED_TO_EDIT_OR_USE_FEATURE;
        return template.builder()
                .addVariablesFor(identifier)
                .buildMessage();
    }

}
