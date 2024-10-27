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
package org.apache.causeway.core.metamodel.specloader.specimpl;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.HasFacetHolder;
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.Hierarchical;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionContainer;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociationContainer;

import lombok.Getter;
import lombok.NonNull;

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
implements
    HasFacetHolder,
    ObjectActionContainer,
    ObjectAssociationContainer,
    Hierarchical {

    @Getter(onMethod_ = {@Override}) private FacetHolder facetHolder;

    protected ObjectMemberContainer(
            final @NonNull MetaModelContext mmc,
            final @NonNull Identifier featureIdentifier) {
        this.facetHolder = FacetHolder.simple(mmc, featureIdentifier);
    }

    // -- ACTIONS

    @Override
    public Optional<ObjectAction> getAction(
            final String id, final ImmutableEnumSet<ActionScope> scopes, final MixedIn mixedIn) {

        var declaredAction = getDeclaredAction(id, mixedIn); // no inheritance nor type considered

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

        var actionStream = isTypeHierarchyRoot()
                ? streamDeclaredActions(actionTypes, mixedIn) // stop going deeper
                : Stream.concat(
                        streamDeclaredActions(actionTypes, mixedIn),
                        superclass().streamActions(actionTypes, mixedIn));

        var actionSignatures = _Sets.<String>newHashSet();
        var actionIds = _Sets.<String>newHashSet();

        return actionStream

        // as of contributing super-classes same actions might appear more than once (overriding)
        .filter(action->{
            if(action.isMixedIn()) {
                return true; // do not filter mixedIn actions based on signature
            }
            var isUnique = actionSignatures
                    .add(action.getFeatureIdentifier().getMemberNameAndParameterClassNamesIdentityString());
            return isUnique;
        })

        // ensure we don't emit duplicates
        .filter(action->{
            var isUnique = actionIds.add(action.getId());
            if(!isUnique) {
                onActionOverloaded.accept(action);
            }
            return isUnique;
        });
    }

    // -- ASSOCIATIONS

    @Override
    public Optional<ObjectAssociation> getAssociation(final String id, final MixedIn mixedIn) {

        var declaredAssociation = getDeclaredAssociation(id, mixedIn); // no inheritance considered

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

        var ids = _Sets.<String>newHashSet();

        return Stream.concat(
                streamDeclaredAssociations(mixedIn),
                superclass().streamAssociations(mixedIn)
        )
        .filter(association->ids.add(association.getId())); // ensure we don't emit duplicates
    }

}
