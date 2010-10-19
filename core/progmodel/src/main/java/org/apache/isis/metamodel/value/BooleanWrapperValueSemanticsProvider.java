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


package org.apache.isis.metamodel.value;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.config.IsisConfiguration;
import org.apache.isis.metamodel.facets.FacetHolder;
import org.apache.isis.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.metamodel.specloader.SpecificationLoader;


public class BooleanWrapperValueSemanticsProvider extends BooleanValueSemanticsProviderAbstract {

    private static final Object DEFAULT_PROVIDER = Boolean.FALSE;

    static final Class<?> adaptedClass() {
        return Boolean.class;
    }

    /**
     * Required because implementation of {@link Parser} and {@link EncoderDecoder}.
     */
    public BooleanWrapperValueSemanticsProvider() {
        this(null, null, null, null);
    }

    public BooleanWrapperValueSemanticsProvider(
    		final FacetHolder holder,
            final IsisConfiguration configuration,
            final SpecificationLoader specificationLoader,
            final RuntimeContext runtimeContext) {
        super(holder, adaptedClass(), DEFAULT_PROVIDER, configuration, specificationLoader, runtimeContext);
    }

    // //////////////////////////////////////////////////////////////////
    // BooleanValueFacet impl
    // //////////////////////////////////////////////////////////////////

    public void reset(final ObjectAdapter adapter) {
        adapter.replacePojo(Boolean.FALSE);
    }

    public void set(final ObjectAdapter adapter) {
        adapter.replacePojo(Boolean.TRUE);
    }

    public void toggle(final ObjectAdapter adapter) {
        final Object currentObj = adapter.getObject();
        if (currentObj == null) {
            set(adapter);
            return;
        }
        final boolean current = ((Boolean) currentObj).booleanValue();
        final boolean toggled = !current;
        adapter.replacePojo(Boolean.valueOf(toggled));
    }

}
