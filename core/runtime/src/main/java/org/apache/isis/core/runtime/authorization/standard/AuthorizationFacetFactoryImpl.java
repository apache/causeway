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


package org.apache.isis.core.runtime.authorization.standard;

import java.lang.reflect.Method;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MethodRemover;
import org.apache.isis.core.metamodel.spec.FacetFactoryAbstract;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;
import org.apache.isis.core.runtime.context.IsisContext;


public class AuthorizationFacetFactoryImpl extends FacetFactoryAbstract {

	public AuthorizationFacetFactoryImpl() {
        super(FeatureType.EVERYTHING_BUT_PARAMETERS);
    }
    
    @Override
    public boolean process(final Class<?> cls, final MethodRemover methodRemover, final FacetHolder holder) {
        return FacetUtil.addFacet(createFacet(holder));
    }

    @Override
    public boolean process(Class<?> cls, final Method method, final MethodRemover methodRemover, final FacetHolder holder) {
        return FacetUtil.addFacet(createFacet(holder));
    }

    private AuthorizationFacetImpl createFacet(final FacetHolder holder) {
    	AuthorizationManager authorizationManager = getAuthorizationManager();
		return new AuthorizationFacetImpl(holder, authorizationManager);
    }

    
    ////////////////////////////////////////////////////////////////////
    // Dependencies (from context)
    ////////////////////////////////////////////////////////////////////
    
	private AuthorizationManager getAuthorizationManager() {
		return IsisContext.getAuthorizationManager();
	}
    
}
