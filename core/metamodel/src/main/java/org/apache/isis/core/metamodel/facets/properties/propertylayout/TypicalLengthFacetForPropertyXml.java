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

package org.apache.isis.core.metamodel.facets.properties.propertylayout;

import java.util.function.BiConsumer;

import org.apache.isis.applib.layout.component.PropertyLayoutData;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.objectvalue.typicallen.TypicalLengthFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.typicallen.TypicalLengthFacetAbstract;

public class TypicalLengthFacetForPropertyXml
extends TypicalLengthFacetAbstract {

    public static TypicalLengthFacet create(PropertyLayoutData propertyLayout, FacetHolder holder) {
        if(propertyLayout == null) {
            return null;
        }
        final Integer typicalLength = propertyLayout.getTypicalLength();
        return typicalLength != null && typicalLength != -1 ? new TypicalLengthFacetForPropertyXml(typicalLength, holder) : null;
    }

    private final int value;

    private TypicalLengthFacetForPropertyXml(int value, FacetHolder holder) {
        super(holder);
        this.value = value;
    }

    @Override
    public int value() {
        return value;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("value", value);
    }

}
