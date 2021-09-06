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
package org.apache.isis.core.metamodel.postprocessors.allbutparam.authorization;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.interactions.ActionVisibilityContext;
import org.apache.isis.core.metamodel.interactions.CollectionVisibilityContext;
import org.apache.isis.core.metamodel.interactions.DisablingInteractionAdvisor;
import org.apache.isis.core.metamodel.interactions.HidingInteractionAdvisor;
import org.apache.isis.core.metamodel.interactions.PropertyVisibilityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.NonNull;

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
                                vc.getHead(),
                                property.getFeatureIdentifier(),
                                vc.getInitiatedBy(),
                                vc.getWhere())) != null)
                .orElse(false);
    }

    public static boolean hidesCollection(
            final @NonNull OneToManyAssociation collection,
            final @NonNull VisibilityContext vc) {

        return collection.lookupFacet(AuthorizationFacet.class)
                .map(facet->facet.hides(
                        new CollectionVisibilityContext(
                                vc.getHead(),
                                collection.getFeatureIdentifier(),
                                vc.getInitiatedBy(),
                                vc.getWhere())) != null)
                .orElse(false);
    }

    public static boolean hidesAction(
            final @NonNull ObjectAction action,
            final @NonNull VisibilityContext vc) {

        return action.lookupFacet(AuthorizationFacet.class)
                .map(facet->facet.hides(
                        new ActionVisibilityContext(
                                vc.getHead(),
                                action,
                                action.getFeatureIdentifier(),
                                vc.getInitiatedBy(),
                                vc.getWhere())) != null)
                .orElse(false);
    }

}
