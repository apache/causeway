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
package org.apache.isis.core.metamodel.specloader.specimpl;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetHolderAbstract;
import org.apache.isis.core.metamodel.spec.ActionScope;
import org.apache.isis.core.metamodel.spec.Hierarchical;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionContainer;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationContainer;

import lombok.val;

/**
 * Responsibility: member lookup and streaming with support for inheritance,
 * based on access to declared members, super-classes and interfaces.
 * <p>
 * TODO performance: add memoization
 * <p>
 * TODO future extensions should also search the interfaces,
 * but avoid doing redundant work when walking the type-hierarchy;
 * (current elegant recursive solution will then need some tweaks to be efficient)
 */
public abstract class ObjectMemberContainer
extends FacetHolderAbstract
implements
    ObjectActionContainer,
    ObjectAssociationContainer,
    Hierarchical {

    protected ObjectMemberContainer(final MetaModelContext metaModelContext) {
        super(metaModelContext);
    }

    // -- ACTIONS

    @Override
    public Optional<ObjectAction> getAction(
            final String id, final ImmutableEnumSet<ActionScope> scopes, final MixedIn mixedIn) {

        val declaredAction = getDeclaredAction(id, mixedIn); // no inheritance nor type considered

        if(declaredAction.isPresent()) {
            // action found but if its not the right type, stop searching
            if(!scopes.contains(declaredAction.get().getScope())) {
                return Optional.empty();
            }
            return declaredAction;
        }

        return isTypeHierarchyRoot()
                ? Optional.empty() // stop searching
                : superclass().getAction(id, scopes, mixedIn);
    }

    @Override
    public Stream<ObjectAction> streamActions(
            final ImmutableEnumSet<ActionScope> actionTypes,
            final MixedIn mixedIn,
            final Consumer<ObjectAction> onActionOverloaded) {

        val actionStream = isTypeHierarchyRoot()
                ? streamDeclaredActions(actionTypes, mixedIn) // stop going deeper
                : Stream.concat(
                        streamDeclaredActions(actionTypes, mixedIn),
                        superclass().streamActions(actionTypes, mixedIn));

        val actionSignatures = _Sets.<String>newHashSet();
        val actionIds = _Sets.<String>newHashSet();

        return actionStream

        // as of contributing super-classes same actions might appear more than once (overriding)
        .filter(action->{
            if(action.isMixedIn()) {
                return true; // do not filter mixedIn actions based on signature
            }
            val isUnique = actionSignatures
                    .add(action.getFeatureIdentifier().getMemberNameAndParameterClassNamesIdentityString());
            return isUnique;
        })

        // ensure we don't emit duplicates
        .filter(action->{
            val isUnique = actionIds.add(action.getId());
            if(!isUnique) {
                onActionOverloaded.accept(action);
            }
            return isUnique;
        });
    }

    // -- ASSOCIATIONS

    @Override
    public Optional<ObjectAssociation> getAssociation(final String id, final MixedIn mixedIn) {

        val declaredAssociation = getDeclaredAssociation(id, mixedIn); // no inheritance considered

        if(declaredAssociation.isPresent()) {
            return declaredAssociation;
        }

        return isTypeHierarchyRoot()
               ? Optional.empty() // stop searching
               : superclass().getAssociation(id, mixedIn);
    }

    @Override
    public Stream<ObjectAssociation> streamAssociations(final MixedIn mixedIn) {

        if(isTypeHierarchyRoot()) {
            return streamDeclaredAssociations(mixedIn); // stop going deeper
        }

        val ids = _Sets.<String>newHashSet();

        return Stream.concat(
                streamDeclaredAssociations(mixedIn),
                superclass().streamAssociations(mixedIn)
        )
        .filter(association->ids.add(association.getId())); // ensure we don't emit duplicates
    }

}
