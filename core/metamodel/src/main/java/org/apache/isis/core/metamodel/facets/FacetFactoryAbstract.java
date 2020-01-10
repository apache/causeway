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

package org.apache.isis.core.metamodel.facets;

import java.util.EnumSet;

import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.context.MetaModelContextAware;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;

import lombok.Getter;
import lombok.Setter;

public abstract class FacetFactoryAbstract 
implements FacetFactory, MetaModelContextAware, MetaModelContext.Delegating {
    
    @Getter(onMethod = @__({@Override})) @Setter(onMethod = @__({@Override}))
    private MetaModelContext metaModelContext;
    
    @Getter(onMethod = @__({@Override}))
    private final EnumSet<FeatureType> featureTypes;

    public FacetFactoryAbstract(EnumSet<FeatureType> featureTypes) {
        this.featureTypes = featureTypes;
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
    }

    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {
    }
    
    // -- FACET UTILITIES

    public void addFacet(final Facet facet) {
        FacetUtil.addFacet(facet);
    }
    
    // -- METHOD UTILITITES
    
    protected static final Class<?> NO_RETURN = (Class<?>)null;
    protected static final Class<?>[] NO_ARG = new Class<?>[0];
    protected static final Class<?>[] STRING_ARG = new Class<?>[] {String.class};

}
