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

package org.apache.isis.core.metamodel.postprocessors.properties;

import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.context.MetaModelContextAware;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.metamodel.facets.object.domainobject.editing.ImmutableFacetFromConfiguration;
import org.apache.isis.core.metamodel.facets.object.immutable.EditingEnabledFacet;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.core.metamodel.facets.properties.disabled.fromimmutable.DisabledFacetOnPropertyDerivedFromImmutable;
import org.apache.isis.core.metamodel.facets.properties.disabled.fromimmutable.DisabledFacetOnPropertyDerivedFromImmutableFactory;
import org.apache.isis.core.metamodel.postprocessors.ObjectSpecificationPostProcessor;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectMemberAbstract;

import lombok.Setter;
import lombok.val;

/**
 * Sets up all the {@link Facet}s for an action in a single shot.
 */
public class DeriveDisabledFromImmutablePostProcessor
implements ObjectSpecificationPostProcessor, MetaModelContextAware {

    @Setter(onMethod = @__(@Override))
    private MetaModelContext metaModelContext;

    @Override
    public void postProcess(final ObjectSpecification objectSpecification) {

        // previously was also copying ImmutableFacet from spec onto Action (as for properties and collections ...
        // corresponds to CopyImmutableFacetOntoMembersFactory.  However, ImmutableFacet only ever disables for
        // properties and collections, so no point in copying over.

        objectSpecification.streamProperties(MixedIn.INCLUDED)
                .forEach(DeriveDisabledFromImmutablePostProcessor::derivePropertyDisabledFromImmutable);
    }


    /**
     * Replaces {@link DisabledFacetOnPropertyDerivedFromImmutableFactory}
     */
    private static void derivePropertyDisabledFromImmutable(final OneToOneAssociation property) {
        if(property.containsNonFallbackFacet(DisabledFacet.class)) {
            return;
        }

        val typeSpec = property.getOnType();

        typeSpec
        .lookupNonFallbackFacet(ImmutableFacet.class)
        .ifPresent(immutableFacet->{

            if(immutableFacet instanceof ImmutableFacetFromConfiguration) {

                val isEditingEnabledOnType = typeSpec.lookupNonFallbackFacet(EditingEnabledFacet.class)
                        .isPresent();

                if(isEditingEnabledOnType) {
                    // @DomainObject(editing=ENABLED)
                    return;
                }

            }

            FacetUtil.addFacet(DisabledFacetOnPropertyDerivedFromImmutable
                            .forImmutable(facetedMethodFor(property), immutableFacet));
        });
    }


    private static FacetedMethod facetedMethodFor(final ObjectMember objectMember) {
        // TODO: hacky, need to copy facet onto underlying peer, not to the action/association itself.
        final ObjectMemberAbstract objectActionImpl = (ObjectMemberAbstract) objectMember;
        return objectActionImpl.getFacetedMethod();
    }


}
