/* Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License. */

package org.apache.isis.core.metamodel.facets.members.cssclassfa.annotprop;

import java.util.Properties;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaFacetAbstract;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaPosition;

public class CssClassFaFacetOnMemberFromProperties extends CssClassFaFacetAbstract {

    public CssClassFaFacetOnMemberFromProperties(final Properties properties, final FacetHolder holder) {
        super(valueFrom(properties), positionFrom(properties), holder);
    }

    private static String valueFrom(Properties properties) {
        return properties.getProperty("value");
    }

    private static CssClassFaPosition positionFrom(final Properties properties) {
        String position = properties.getProperty("position");
        return CssClassFaPosition.valueOf(position.toUpperCase());
    }
}
