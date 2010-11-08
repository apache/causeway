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


package org.apache.isis.metamodel.facets.actions.executed;

import java.lang.reflect.Method;

import org.apache.isis.core.metamodel.spec.feature.ObjectFeatureType;
import org.apache.isis.metamodel.facets.Facet;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.facets.FacetHolder;
import org.apache.isis.metamodel.facets.FacetUtil;
import org.apache.isis.metamodel.facets.MethodRemover;
import org.apache.isis.metamodel.facets.actions.ActionMethodsFacetFactory;
import org.apache.isis.metamodel.facets.actions.ExecutedFacetViaNamingConvention;
import org.apache.isis.metamodel.facets.naming.named.NamedFacetInferred;


/**
 * Creates an {@link ExecutedFacet} based on the prefix of the action's name.
 * 
 * <p>
 * TODO: think that this prefix is handled by the {@link ActionMethodsFacetFactory}.
 */
public class ExecutedViaNamingConventionFacetFactory extends FacetFactoryAbstract {

    private static final String LOCAL_PREFIX = "Local";

    public ExecutedViaNamingConventionFacetFactory() {
        super(ObjectFeatureType.ACTIONS_ONLY);
    }

    @Override
    public boolean process(Class<?> cls, final Method method, final MethodRemover methodRemover, final FacetHolder holder) {
        final String fullMethodName = method.getName();
        final String capitalizedName = fullMethodName.substring(0, 1).toUpperCase() + fullMethodName.substring(1);

        if (!capitalizedName.startsWith(LOCAL_PREFIX)) {
            return false;
        }

        return FacetUtil.addFacets(new Facet[] { new ExecutedFacetViaNamingConvention(ExecutedFacet.Where.LOCALLY, holder),
                new NamedFacetInferred(capitalizedName.substring(5), holder) });
    }

    public boolean recognizes(final Method method) {
        return method.getName().startsWith(LOCAL_PREFIX);
    }

}
