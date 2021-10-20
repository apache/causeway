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
package org.apache.isis.core.metamodel.facets.param.typicallen.fromtype;

import java.util.function.BiConsumer;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.objectvalue.multiline.MultiLineFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.typicallen.TypicalLengthFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.typicallen.TypicalLengthFacetAbstract;

public class TypicalLengthFacetOnParameterInferredFromType
extends TypicalLengthFacetAbstract {

    // -- FACTORIES

    /**
     * @apiNote call during post processing only!
     * based on the assumption, that all MultiLineFacet processing already has settled
     * on the peer (action parameter meta-data)
     */
    public static TypicalLengthFacetOnParameterInferredFromType createWhilePostprocessing(
            final TypicalLengthFacet typicalLengthFacet,
            final FacetHolder holder) {

        final int numberOfLines = holder.lookupFacet(MultiLineFacet.class)
                .map(MultiLineFacet::numberOfLines)
                .orElse(1);
        final int typicalLength = numberOfLines * typicalLengthFacet.value();

        return new TypicalLengthFacetOnParameterInferredFromType(typicalLength, typicalLengthFacet, holder);
    }

    // -- FIELDS

    /**
     * @apiNote held only for reporting purposes
     */
    private final TypicalLengthFacet typicalLengthFacet;

    // -- CONSTRUCTOR

    private TypicalLengthFacetOnParameterInferredFromType(
            final int typicalLength,
            final TypicalLengthFacet typicalLengthFacet,
            final FacetHolder holder) {
        super(typicalLength, holder, Precedence.INFERRED);
        this.typicalLengthFacet = typicalLengthFacet;
    }

    // -- IMPL

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("typicalLengthFacet", typicalLengthFacet);
    }

}
