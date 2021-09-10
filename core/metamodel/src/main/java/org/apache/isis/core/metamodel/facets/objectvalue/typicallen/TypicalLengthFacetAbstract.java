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
package org.apache.isis.core.metamodel.facets.objectvalue.typicallen;

import java.util.function.BiConsumer;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.SingleIntValueFacetAbstract;

public abstract class TypicalLengthFacetAbstract
extends SingleIntValueFacetAbstract
implements TypicalLengthFacet {

    public static final Class<TypicalLengthFacet> type() {
        return TypicalLengthFacet.class;
    }

    protected TypicalLengthFacetAbstract(
            final int typicalLength,
            final FacetHolder holder) {
        super(type(), holder, typicalLength);
    }

    protected TypicalLengthFacetAbstract(
            final int typicalLength,
            final FacetHolder holder,
            final Facet.Precedence precedence) {
        super(type(), holder, typicalLength, precedence);
    }

    // -- REPORTING

    @Override
    protected String getAttributeNameForValue() {
        return "typicalLength";
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        final int typicalLength = value();
        visitor.accept("typicalLength", typicalLength == 0
                ? "default"
                : String.valueOf(typicalLength));
    }


}
