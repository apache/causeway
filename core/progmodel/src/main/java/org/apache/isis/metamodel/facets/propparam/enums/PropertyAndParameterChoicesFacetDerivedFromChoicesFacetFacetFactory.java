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


package org.apache.isis.metamodel.facets.propparam.enums;

import java.lang.reflect.Method;

import org.apache.isis.core.metamodel.spec.feature.ObjectFeatureType;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.facets.FacetHolder;
import org.apache.isis.metamodel.facets.FacetUtil;
import org.apache.isis.metamodel.facets.MethodRemover;

public class PropertyAndParameterChoicesFacetDerivedFromChoicesFacetFacetFactory extends
    FacetFactoryAbstract {


    public PropertyAndParameterChoicesFacetDerivedFromChoicesFacetFacetFactory() {
        super(ObjectFeatureType.PROPERTIES_AND_PARAMETERS);
    }

    @Override
    public boolean process(Class<?> cls, Method method,
            MethodRemover methodRemover, FacetHolder holder) {

        Class<?> returnType = method.getReturnType();
        
        if (!returnType.isEnum()) {
            return false;
        }
        
        FacetUtil.addFacet(new PropertyChoicesFacetDerivedFromChoicesFacet(holder));
        return true;
    }
    
    @Override
    public boolean processParams(Method method, int paramNum, FacetHolder holder) {
        Class<?> paramType = method.getParameterTypes()[paramNum];
        
        if (!paramType.isEnum()) {
            return false;
        }
        
        FacetUtil.addFacet(new ActionParameterChoicesFacetDerivedFromChoicesFacet(holder));
        return true;
    }

}
