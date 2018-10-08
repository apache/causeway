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

package org.apache.isis.core.metamodel.facets.members.cssclassfa;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.SingleStringValueFacetAbstract;

public class CssClassFaFacetAbstract extends SingleStringValueFacetAbstract implements CssClassFaFacet {

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private CssClassFaPosition position;

    public CssClassFaFacetAbstract(final String value, final CssClassFaPosition position,
            final FacetHolder holder) {
        super(type(), holder, sanitize(value));
        this.position = position;
    }

    @Override
    public CssClassFaPosition getPosition() {
        return position;
    }

    /**
     * Adds the optional <em>fa</em> and <em>fa-fw</em> FontAwesome classes
     *
     * @param value The original CSS classes defined with {@literal @}
     *            {@link org.apache.isis.applib.annotation.CssClassFa CssClassFa}
     * @return The original CSS classes plus <em>fa</em> and <em>fa-fw</em> if not already provided
     */
    static String sanitize(final String value) {
        final Iterable<String> classes = Splitter.on(WHITESPACE).split(value.trim());
        final Set<String> cssClassesSet = Sets.newLinkedHashSet();
        cssClassesSet.add("fa");
        cssClassesSet.add("fa-fw");
        for (final String cssClass : classes) {
            cssClassesSet.add(faPrefix(cssClass));
        }
        return Joiner.on(' ').join(cssClassesSet).trim();
    }

    private static String faPrefix(final String cssClass) {
        return cssClass.startsWith("fa-") || "fa".equals(cssClass)
                ? cssClass
                : "fa-" + cssClass;
    }

    public static Class<? extends Facet> type() {
        return CssClassFaFacet.class;
    }

    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("position", position);
    }
}
