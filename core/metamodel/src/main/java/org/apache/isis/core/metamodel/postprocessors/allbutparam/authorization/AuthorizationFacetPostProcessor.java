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

import java.util.stream.Stream;

import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.context.MetaModelContextAware;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.progmodel.ObjectSpecificationPostProcessor;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

import lombok.Setter;
import lombok.val;

public class AuthorizationFacetPostProcessor
    implements ObjectSpecificationPostProcessor, MetaModelContextAware {

    @Setter(onMethod = @__(@Override))
    private MetaModelContext metaModelContext;

    @Override
    public void postProcess(ObjectSpecification objectSpecification) {

        objectSpecification.addFacet(createFacet(objectSpecification));

        val actionTypes = inferActionTypes();
        objectSpecification.streamActions(actionTypes, MixedIn.INCLUDED)
                .forEach(act -> act.addFacet(createFacet(act)));

        objectSpecification.streamProperties(MixedIn.INCLUDED).
                forEach(prop -> {prop.addFacet(createFacet(prop));});

        objectSpecification.streamCollections(MixedIn.INCLUDED).
                forEach(coll -> {coll.addFacet(createFacet(coll));});

    }

    private AuthorizationFacetImpl createFacet(final FacetHolder holder) {
        return new AuthorizationFacetImpl(holder);
    }

    private ImmutableEnumSet<ActionType> inferActionTypes() {
        return metaModelContext.getSystemEnvironment().isPrototyping()
                ? ActionType.USER_AND_PROTOTYPE
                : ActionType.USER_ONLY;
    }

}
