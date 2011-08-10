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

package org.apache.isis.core.progmodel.facets.object.title.annotation;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.util.AdapterInvokeUtils;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacetAbstract;
import org.apache.log4j.Logger;

public class TitleFacetViaTitleAnnotation extends TitleFacetAbstract implements ImperativeFacet {

    private static final Logger LOG = Logger.getLogger(TitleFacetViaTitleAnnotation.class);
    private final List<Method> methods;

    public TitleFacetViaTitleAnnotation(final List<Method> methods, final FacetHolder holder) {
        super(holder);
        this.methods = methods;
    }

    /**
     * Returns a singleton list of the {@link Method}(s) provided in the constructor.
     */
    @Override
    public List<Method> getMethods() {
        return Collections.unmodifiableList(methods);
    }

    @Override
    public boolean impliesResolve() {
        return true;
    }

    @Override
    public boolean impliesObjectChanged() {
        return false;
    }

    @Override
    public String title(final ObjectAdapter owningAdapter, final Localization localization) {
    	StringBuilder stringBuilder = new StringBuilder();

        try {
        	for (Method method : this.methods) {
        		stringBuilder.append((String) AdapterInvokeUtils.invoke(method, owningAdapter)).append(' ');
        	}

        	return stringBuilder.toString().trim();
        } catch (final RuntimeException ex) {
            LOG.warn("Title failure", ex);
            return "Failed Title";
        }
    }

}
