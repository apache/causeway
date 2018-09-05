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

package org.apache.isis.core.metamodel.facets.value.booleans;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.properties.defaults.PropertyDefaultFacet;
import org.apache.isis.core.metamodel.services.ServicesInjector;


public class BooleanPrimitiveValueSemanticsProvider extends BooleanValueSemanticsProviderAbstract implements PropertyDefaultFacet {

    private static final Boolean DEFAULT_VALUE = Boolean.FALSE;

    /**
     * Required because implementation of {@link Parser} and
     * {@link EncoderDecoder}.
     */
    public BooleanPrimitiveValueSemanticsProvider() {
        this(null, null);
    }

    public BooleanPrimitiveValueSemanticsProvider(final FacetHolder holder, final ServicesInjector context) {
        super(holder, boolean.class, DEFAULT_VALUE, context);
    }

    // //////////////////////////////////////////////////////////////////
    // PropertyDefault
    // //////////////////////////////////////////////////////////////////

    @Override
    public ObjectAdapter getDefault(final ObjectAdapter inObject) {
        return createAdapter(boolean.class, Boolean.FALSE);
    }

    // //////////////////////////////////////////////////////////////////
    // BooleanValueFacet impl
    // //////////////////////////////////////////////////////////////////

//    @Override
//    public void reset(final ObjectAdapter object) {
//        object.replacePojo(Boolean.FALSE);
//    }
//
//    @Override
//    public void set(final ObjectAdapter object) {
//        object.replacePojo(Boolean.TRUE);
//    }
//
//    @Override
//    public void toggle(final ObjectAdapter object) {
//        final boolean current = ((Boolean) object.getObject()).booleanValue();
//        final boolean toggled = !current;
//        object.replacePojo(Boolean.valueOf(toggled));
//    }

}
