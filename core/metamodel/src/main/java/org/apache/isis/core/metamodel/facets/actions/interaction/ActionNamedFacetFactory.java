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

package org.apache.isis.core.metamodel.facets.actions.interaction;

import java.lang.reflect.Method;

import org.apache.isis.core.commons.lang.StringExtensions;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacetInferred;

/**
 * Sets up {@link org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacet} and also an {@link org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet}, along with a number of supporting facets that are based on the action's name.
 *
 * <p>
 * In addition a {@link NamedFacet} is inferred from the name.
 *
 * @deprecated
 */
@Deprecated
public class ActionNamedFacetFactory extends MethodPrefixBasedFacetFactoryAbstract {

    public ActionNamedFacetFactory() {
        super(FeatureType.ACTIONS_ONLY, OrphanValidation.VALIDATE);
    }

    // ///////////////////////////////////////////////////////
    // Actions
    // ///////////////////////////////////////////////////////

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        attachNamedFacetInferredFromMethodName(processMethodContext);
    }



    /**
     * Must be called after added the debug, exploration etc facets.
     */
    private void attachNamedFacetInferredFromMethodName(final ProcessMethodContext processMethodContext) {

        final Method method = processMethodContext.getMethod();

        String name = StringExtensions.asCapitalizedName(method.getName());
        name = StringExtensions.asNaturalName2(name);

        final FacetHolder facetedMethod = processMethodContext.getFacetHolder();
        FacetUtil.addFacet(new NamedFacetInferred(name, facetedMethod));
    }

}
