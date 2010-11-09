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


package org.apache.isis.core.progmodel.value;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.facets.properties.defaults.PropertyDefaultFacet;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;


public class BooleanPrimitiveValueSemanticsProvider extends BooleanValueSemanticsProviderAbstract implements PropertyDefaultFacet {

    private static final Object DEFAULT_VALUE = Boolean.FALSE;

    static final Class<?> adaptedClass() {
        return boolean.class;
    }

    /**
     * Required because implementation of {@link Parser} and {@link EncoderDecoder}.
     */
    public BooleanPrimitiveValueSemanticsProvider() {
        this(null, null, null, null);
    }

    public BooleanPrimitiveValueSemanticsProvider(
    		final FacetHolder holder,
            final IsisConfiguration configuration, 
            final SpecificationLoader specificationLoader, 
            final RuntimeContext runtimeContext) {
        super(holder, adaptedClass(), DEFAULT_VALUE, configuration, specificationLoader, runtimeContext);
    }

    // //////////////////////////////////////////////////////////////////
    // PropertyDefault
    // //////////////////////////////////////////////////////////////////

    public ObjectAdapter getDefault(final ObjectAdapter inObject) {
        return createAdapter(boolean.class, Boolean.FALSE);
    }

    // //////////////////////////////////////////////////////////////////
    // BooleanValueFacet impl
    // //////////////////////////////////////////////////////////////////

    public void reset(final ObjectAdapter object) {
        object.replacePojo(Boolean.FALSE);
    }

    public void set(final ObjectAdapter object) {
        object.replacePojo(Boolean.TRUE);
    }

    public void toggle(final ObjectAdapter object) {
        final boolean current = ((Boolean) object.getObject()).booleanValue();
        final boolean toggled = !current;
        object.replacePojo(Boolean.valueOf(toggled));
    }


}
