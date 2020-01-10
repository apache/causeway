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

package org.apache.isis.core.metamodel.facets.value.doubles;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.core.metamodel.facets.properties.defaults.PropertyDefaultFacet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.spec.ManagedObject;


public class DoublePrimitiveValueSemanticsProvider
extends DoubleValueSemanticsProviderAbstract
implements PropertyDefaultFacet {

    /**
     * Required because implementation of {@link Parser} and
     * {@link EncoderDecoder}.
     */
    public DoublePrimitiveValueSemanticsProvider() {
        this(null);
    }

    public DoublePrimitiveValueSemanticsProvider(final FacetHolder holder) {
        super(holder, double.class);
    }

    // //////////////////////////////////////////////////////////////////
    // PropertyDefaultFacet
    // //////////////////////////////////////////////////////////////////

    @Override
    public ManagedObject getDefault(final ManagedObject inObject) {
        return createAdapter(double.class, Double.valueOf(0.0));
    }

}
