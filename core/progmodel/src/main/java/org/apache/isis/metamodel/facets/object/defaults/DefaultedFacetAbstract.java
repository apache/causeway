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


package org.apache.isis.metamodel.facets.object.defaults;

import org.apache.isis.applib.adapters.DefaultsProvider;
import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.core.metamodel.util.ClassUtil;
import org.apache.isis.metamodel.facets.FacetAbstract;
import org.apache.isis.metamodel.facets.FacetHolder;
import org.apache.isis.metamodel.runtimecontext.RuntimeContext;


public abstract class DefaultedFacetAbstract extends FacetAbstract implements DefaultedFacet {

    private final Class<?> defaultsProviderClass;

    // to delegate to
    private final DefaultedFacetUsingDefaultsProvider defaultedFacetUsingDefaultsProvider;
    private final RuntimeContext runtimeContext;

    public DefaultedFacetAbstract(
            final String candidateEncoderDecoderName,
            final Class<?> candidateEncoderDecoderClass,
            final FacetHolder holder, 
            final RuntimeContext runtimeContext) {
        super(DefaultedFacet.class, holder, false);

        this.defaultsProviderClass = DefaultsProviderUtil.defaultsProviderOrNull(candidateEncoderDecoderClass,
                candidateEncoderDecoderName);
        this.runtimeContext = runtimeContext;
        if (isValid()) {
            DefaultsProvider defaultsProvider = (DefaultsProvider) ClassUtil.newInstance(defaultsProviderClass, FacetHolder.class, holder);
            this.defaultedFacetUsingDefaultsProvider = new DefaultedFacetUsingDefaultsProvider(defaultsProvider, holder, getRuntimeContext());
        } else {
            this.defaultedFacetUsingDefaultsProvider = null;
        }
    }

    /**
     * Discover whether either of the candidate defaults provider name or class is valid.
     */
    public boolean isValid() {
        return defaultsProviderClass != null;
    }

    /**
     * Guaranteed to implement the {@link EncoderDecoder} class, thanks to generics in the applib.
     */
    public Class<?> getDefaultsProviderClass() {
        return defaultsProviderClass;
    }

    public Object getDefault() {
        return defaultedFacetUsingDefaultsProvider.getDefault();
    }

    @Override
    protected String toStringValues() {
        return defaultsProviderClass.getName();
    }

    ////////////////////////////////////////////////////////
    // Dependencies (from constructor)
    ////////////////////////////////////////////////////////
    

    private RuntimeContext getRuntimeContext() {
        return runtimeContext;
    }

}

