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
package org.apache.causeway.core.metamodel.postprocessors.properties;

import jakarta.inject.Inject;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.causeway.core.metamodel.facets.object.domainobject.editing.ImmutableFacetFromConfiguration;
import org.apache.causeway.core.metamodel.facets.object.immutable.EditingEnabledFacet;
import org.apache.causeway.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.causeway.core.metamodel.facets.properties.disabled.fromimmutable.DisabledFacetOnPropertyFromImmutable;
import org.apache.causeway.core.metamodel.facets.properties.disabled.fromimmutable.DisabledFacetOnPropertyFromImmutableFactory;
import org.apache.causeway.core.metamodel.postprocessors.MetaModelPostProcessorAbstract;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

/**
 * Replaces {@link DisabledFacetOnPropertyFromImmutableFactory}
 */
public class DisabledFromImmutablePostProcessor
extends MetaModelPostProcessorAbstract {

    @Inject
    public DisabledFromImmutablePostProcessor(final MetaModelContext metaModelContext) {
        super(metaModelContext);
    }

    @Override
    public void postProcessAction(final ObjectSpecification objectSpecification, final ObjectAction act) {
        // previously was also copying ImmutableFacet from spec onto Action (as for properties and collections ...
        // corresponds to CopyImmutableFacetOntoMembersFactory.  However, ImmutableFacet only ever disables for
        // properties and collections, so no point in copying over.
    }

    @Override
    public void postProcessProperty(final ObjectSpecification objectSpecification, final OneToOneAssociation property) {
        if(property.containsNonFallbackFacet(DisabledFacet.class)) {
            return;
        }

        var typeSpec = property.getDeclaringType();

        typeSpec
        .lookupNonFallbackFacet(ImmutableFacet.class)
        .ifPresent(immutableFacet->{

            if(immutableFacet instanceof ImmutableFacetFromConfiguration) {

                var isEditingEnabledOnType = typeSpec.lookupNonFallbackFacet(EditingEnabledFacet.class)
                        .isPresent();

                if(isEditingEnabledOnType) {
                    // @DomainObject(editing=ENABLED)
                    return;
                }

            }

            FacetUtil.addFacet(DisabledFacetOnPropertyFromImmutable
                            .forImmutable(facetedMethodFor(property), immutableFacet));
        });
    }

}
