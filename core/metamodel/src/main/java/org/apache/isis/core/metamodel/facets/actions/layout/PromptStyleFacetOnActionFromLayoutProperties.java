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

package org.apache.isis.core.metamodel.facets.actions.layout;

import java.util.Map;
import java.util.Properties;

import com.google.common.base.Strings;

import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.isis.core.metamodel.facets.object.promptStyle.PromptStyleFacetAbstract;

public class PromptStyleFacetOnActionFromLayoutProperties extends PromptStyleFacetAbstract {

    private final PromptStyle promptStyle;

    public static PromptStyleFacet create(Properties properties, FacetHolder holder) {
        final PromptStyle promptStyle = promptStyle(properties);
        return promptStyle != null? new PromptStyleFacetOnActionFromLayoutProperties(promptStyle, holder): null;
    }

    private PromptStyleFacetOnActionFromLayoutProperties(PromptStyle promptStyle, FacetHolder holder) {
        super(holder);
        this.promptStyle = promptStyle;
    }

    private static PromptStyle promptStyle(Properties properties) {
        if(properties == null) {
            return null;
        }
        String propertyPromptStyle = Strings.emptyToNull(properties.getProperty("promptStyle"));
        if(propertyPromptStyle == null) {
            return null;
        }
        return PromptStyle.valueOf(propertyPromptStyle);
    }

    @Override
    public PromptStyle value() {
        return promptStyle;
    }

    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("promptStyle", promptStyle);
    }
}
