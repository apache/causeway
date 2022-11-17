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
package org.apache.causeway.core.metamodel.facets.object.value.vsp;

import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.object.value.ValueFacetAbstract;

public class ValueFacetUsingSemanticsProvider<T>
extends ValueFacetAbstract<T> {

    public static <T> ValueFacetUsingSemanticsProvider<T> create(
            final Class<T> valueClass,
            final Can<ValueSemanticsProvider<T>> valueSemantics,
            final FacetHolder holder) {
        return new ValueFacetUsingSemanticsProvider<T>(valueClass, valueSemantics, holder);
    }

    protected ValueFacetUsingSemanticsProvider(
            final Class<T> valueClass,
            final Can<ValueSemanticsProvider<T>> valueSemantics,
            final FacetHolder holder) {
        super(valueClass, valueSemantics, holder, Precedence.DEFAULT);
    }

}
