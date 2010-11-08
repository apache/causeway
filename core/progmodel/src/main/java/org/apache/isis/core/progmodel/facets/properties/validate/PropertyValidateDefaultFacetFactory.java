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


package org.apache.isis.core.progmodel.facets.properties.validate;

import java.lang.reflect.Method;

import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.facets.FacetUtil;
import org.apache.isis.core.metamodel.facets.MethodRemover;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeatureType;
import org.apache.isis.core.metamodel.specloader.internal.facetprocessor.FacetProcessor;


/**
 * Simply installs a {@link PropertyValidateFacet} onto all properties.
 * 
 * <p>
 * The idea is that this {@link FacetFactory} is included early on in the {@link FacetProcessor}, but
 * other {@link PropertyValidateFacet} implementations will potentially replace these where the property is
 * annotated or otherwise provides a validation mechanism.
 */
public class PropertyValidateDefaultFacetFactory extends FacetFactoryAbstract {

    public PropertyValidateDefaultFacetFactory() {
        super(ObjectFeatureType.PROPERTIES_ONLY);
    }

    @Override
    public boolean process(Class<?> cls, final Method method, final MethodRemover methodRemover, final FacetHolder holder) {
        return FacetUtil.addFacet(create(holder));
    }

    @Override
    public boolean processParams(final Method method, final int paramNum, final FacetHolder holder) {
        return FacetUtil.addFacet(create(holder));
    }

    private PropertyValidateFacet create(final FacetHolder holder) {
        return new PropertyValidateFacetDefault(holder);
    }

}
