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

import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.layout.component.PropertyLayoutData;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.isis.core.metamodel.facets.object.promptStyle.PromptStyleFacetAbstract;

public class PromptStyleFacetForPropertyXml extends PromptStyleFacetAbstract {

    public static PromptStyleFacet create(PropertyLayoutData propertyLayout, FacetHolder holder) {
        if(propertyLayout == null) {
            return null;
        }
        final PromptStyle promptStyle = propertyLayout.getPromptStyle();
        return promptStyle != null ? new PromptStyleFacetForPropertyXml(promptStyle, holder) : null;
    }

    private final PromptStyle promptStyle;

    private PromptStyleFacetForPropertyXml(PromptStyle promptStyle, FacetHolder holder) {
        super(holder);
        this.promptStyle = promptStyle;
    }

    @Override
    public PromptStyle value() {
        return promptStyle;
    }

    @Override public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("promptStyle", promptStyle);
    }

}
