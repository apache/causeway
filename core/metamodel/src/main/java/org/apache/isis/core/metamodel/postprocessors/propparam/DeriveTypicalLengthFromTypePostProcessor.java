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

package org.apache.isis.core.metamodel.postprocessors.propparam;

import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.objectvalue.typicallen.TypicalLengthFacet;
import org.apache.isis.core.metamodel.facets.param.typicallen.fromtype.TypicalLengthFacetOnParameterDerivedFromType;
import org.apache.isis.core.metamodel.facets.param.typicallen.fromtype.TypicalLengthFacetOnParameterDerivedFromTypeFacetFactory;
import org.apache.isis.core.metamodel.facets.properties.typicallen.fromtype.TypicalLengthFacetOnPropertyDerivedFromType;
import org.apache.isis.core.metamodel.facets.properties.typicallen.fromtype.TypicalLengthFacetOnPropertyDerivedFromTypeFacetFactory;
import org.apache.isis.core.metamodel.postprocessors.ObjectSpecificationPostProcessorAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

/**
 * replaces {@link TypicalLengthFacetOnPropertyDerivedFromTypeFacetFactory}
 * and {@link TypicalLengthFacetOnParameterDerivedFromTypeFacetFactory}
 */
public class DeriveTypicalLengthFromTypePostProcessor
extends ObjectSpecificationPostProcessorAbstract {

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification) {
    }

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification, ObjectAction act) {
    }

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification, ObjectAction objectAction, final ObjectActionParameter parameter) {
        if(parameter.containsNonFallbackFacet(TypicalLengthFacet.class)) {
            return;
        }
        parameter.getSpecification()
        .lookupNonFallbackFacet(TypicalLengthFacet.class)
        .ifPresent(specFacet -> FacetUtil.addFacetIfPresent(new TypicalLengthFacetOnParameterDerivedFromType(specFacet,
                                    peerFor(parameter))));
    }

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification, final OneToOneAssociation property) {
        if(property.containsNonFallbackFacet(TypicalLengthFacet.class)) {
            return;
        }
        property.getSpecification()
        .lookupNonFallbackFacet(TypicalLengthFacet.class)
        .ifPresent(specFacet -> FacetUtil.addFacetIfPresent(new TypicalLengthFacetOnPropertyDerivedFromType(
                                    specFacet, facetedMethodFor(property))));

    }

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification, OneToManyAssociation coll) {
    }


}
