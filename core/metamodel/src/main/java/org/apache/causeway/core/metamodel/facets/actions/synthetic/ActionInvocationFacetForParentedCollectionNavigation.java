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
package org.apache.causeway.core.metamodel.facets.actions.synthetic;


import org.apache.causeway.applib.exceptions.RecoverableException;
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.iactn.InteractionProvider;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.IdentifierUtil;
import org.apache.causeway.core.metamodel.facets.members.publish.command.CommandPublishingFacet;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.services.publishing.CommandPublisher;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;

import lombok.NonNull;
import lombok.val;

public class ActionInvocationFacetForParentedCollectionNavigation
extends FacetAbstract
implements ActionInvocationFacet {

    private static Class<? extends Facet> type() {
        return ActionInvocationFacet.class;
    }

    private final @NonNull ObjectSpecification declaringType;
    private final @NonNull ObjectSpecification returnType;
    private final @NonNull OneToManyAssociation collection;
    private final @NonNull Can<ObjectAssociation> filterProperties;

    public ActionInvocationFacetForParentedCollectionNavigation(
            final @NonNull ObjectSpecification declaringType,
            final @NonNull ObjectSpecification returnType,
            final @NonNull OneToManyAssociation collection,
            final @NonNull Can<ObjectAssociation> filterProperties,
            final @NonNull org.apache.causeway.core.metamodel.facetapi.FacetHolder holder) {
        super(type(), holder);
        this.declaringType = declaringType;
        this.returnType = returnType;
        this.collection = collection;
        this.filterProperties = filterProperties;
    }

    @Override
    public ObjectSpecification getDeclaringType() {
        return declaringType;
    }

    @Override
    public ObjectSpecification getReturnType() {
        return returnType;
    }

    @Override
    public ManagedObject invoke(
            final ObjectAction owningAction,
            final InteractionHead head,
            final Can<ManagedObject> argumentAdapters,
            final InteractionInitiatedBy interactionInitiatedBy) {

        val command = commandIfAny();
        prepareCommandForPublishing(command, head, owningAction, interactionInitiatedBy);

        val matchResult = ParentedCollectionNavigationMatchingUtil.match(
                collection,
                filterProperties,
                head.getTarget(),
                argumentAdapters,
                interactionInitiatedBy);
        val validationMessage = ParentedCollectionNavigationMatchingUtil.validationMessage(owningAction.getId(), matchResult);
        if(validationMessage != null) {
            throw new RecoverableException(validationMessage);
        }
        val singleMatch = matchResult.singleMatch();
        setCommandResultIfBookmarkable(command, singleMatch, interactionInitiatedBy);
        return singleMatch;
    }

    private Command commandIfAny() {
        return lookupService(InteractionProvider.class)
                .flatMap(InteractionProvider::currentInteraction)
                .map(interaction -> interaction.getCommand())
                .orElse(null);
    }

    private void prepareCommandForPublishing(
            final Command command,
            final InteractionHead head,
            final ObjectAction owningAction,
            final InteractionInitiatedBy interactionInitiatedBy) {
        if(command == null || interactionInitiatedBy.isPassThrough()) {
            return;
        }
        if(IdentifierUtil.isCommandForMember(command, head, owningAction)
                && CommandPublishingFacet.isPublishingEnabled(facetHolder())) {
            command.updater().setPublishingPhase(Command.CommandPublishingPhase.READY);
        }
        lookupService(CommandPublisher.class)
                .ifPresent(commandPublisher -> commandPublisher.ready(command));
    }

    private void setCommandResultIfBookmarkable(
            final Command command,
            final ManagedObject result,
            final InteractionInitiatedBy interactionInitiatedBy) {
        if(command == null || interactionInitiatedBy.isPassThrough() || command.getResult() != null) {
            return;
        }
        val entityState = result.getEntityState();
        if(!entityState.isPersistable() || !entityState.hasOid()) {
            return;
        }
        result.getBookmark()
                .ifPresent(bookmark -> command.updater().setResult(Try.success(bookmark)));
    }

}
