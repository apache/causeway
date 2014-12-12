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

import java.util.Properties;
import com.google.common.base.Strings;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaFacet;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaFacetAbstract;

public class CssClassFaFacetOnActionFromLayoutProperties extends CssClassFaFacetAbstract {

    public static CssClassFaFacet create(Properties properties, FacetHolder holder) {
        final String cssClassFa = cssClassFa(properties);
        ActionLayout.ClassFaPosition position = cssClassFaPosition(properties);
        return cssClassFa != null? new CssClassFaFacetOnActionFromLayoutProperties(cssClassFa, position, holder): null;
    }

    private CssClassFaFacetOnActionFromLayoutProperties(String cssClass, ActionLayout.ClassFaPosition position, FacetHolder holder) {
        super(cssClass, position, holder);
    }

    private static String cssClassFa(Properties properties) {
        if(properties == null) {
            return null;
        }
        return Strings.emptyToNull(properties.getProperty("cssClassFa"));
    }

    private static ActionLayout.ClassFaPosition cssClassFaPosition(Properties properties) {
        if(properties == null) {
            return null;
        }
        String cssClassFaPosition = Strings.emptyToNull(properties.getProperty("cssClassFaPosition"));
        return cssClassFaPosition != null
            ? ActionLayout.ClassFaPosition.valueOf(cssClassFaPosition.toUpperCase())
            : ActionLayout.ClassFaPosition.LEFT;
    }
}
