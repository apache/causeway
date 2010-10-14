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


package org.apache.isis.metamodel.facets.actions;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.apache.isis.metamodel.facets.FacetHolder;
import org.apache.isis.metamodel.facets.naming.describedas.DescribedAsFacetAbstract;
import org.apache.isis.metamodel.java5.ImperativeFacet;


public class DescribedAsFacetViaMethod extends DescribedAsFacetAbstract implements ImperativeFacet {

    private final Method method;

    public DescribedAsFacetViaMethod(
    		final String value, 
    		final Method method, 
    		final FacetHolder holder) {
        super(value, holder);
        this.method = method;
    }

    public List<Method> getMethods() {
    	return Collections.singletonList(method);
    }


	public boolean impliesResolve() {
		return false;
	}

	public boolean impliesObjectChanged() {
		return false;
	}

}

