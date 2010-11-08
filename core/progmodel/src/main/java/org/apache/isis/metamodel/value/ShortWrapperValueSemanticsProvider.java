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
import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.metamodel.facets.FacetHolder;
import org.apache.isis.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.metamodel.specloader.SpecificationLoader;


public class ShortWrapperValueSemanticsProvider extends ShortValueSemanticsProviderAbstract {

    private static final Object DEFAULT_VALUE = Short.valueOf((short) 0);

    static final Class<?> adaptedClass() {
        return Short.class;
    }

    /**
     * Required because implementation of {@link Parser} and {@link EncoderDecoder}.
     */
    public ShortWrapperValueSemanticsProvider() {
        this(null, null, null, null);
    }

    public ShortWrapperValueSemanticsProvider(
    		final FacetHolder holder,
            final IsisConfiguration configuration,
            final SpecificationLoader specificationLoader,
            final RuntimeContext runtimeContext) {
        super(holder, adaptedClass(), DEFAULT_VALUE, configuration, specificationLoader, runtimeContext);
    }

}
