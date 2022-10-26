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

import javax.inject.Inject;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.postprocessors.ObjectSpecificationPostProcessorAbstract;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

public class AuthorizationPostProcessor
    extends ObjectSpecificationPostProcessorAbstract {

    @Inject
    public AuthorizationPostProcessor(final MetaModelContext metaModelContext) {
        super(metaModelContext);
    }

    @Override
    public void postProcessObject(final ObjectSpecification objectSpecification) {
        addFacet(objectSpecification);
    }

    @Override
    public void postProcessAction(final ObjectSpecification objectSpecification, final ObjectAction act) {
        addFacet(act);
    }

    @Override
    public void postProcessProperty(final ObjectSpecification objectSpecification, final OneToOneAssociation prop) {
        addFacet(prop);
    }

    @Override
    public void postProcessCollection(final ObjectSpecification objectSpecification, final OneToManyAssociation coll) {
        addFacet(coll);
    }

    private static void addFacet(final FacetHolder facetHolder) {
        facetHolder.addFacet(new AuthorizationFacetImpl(facetHolder));
    }

}
