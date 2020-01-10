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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.commons.internal.collections._Sets;
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
        final Stream<String> classes = _Strings.splitThenStream(value.trim(), WHITESPACE);
        final Set<String> cssClassesSet = _Sets.newLinkedHashSet();
        cssClassesSet.add("fa");
        cssClassesSet.add("fa-fw");
        classes.forEach(cssClass->cssClassesSet.add(faPrefix(cssClass)));
        return cssClassesSet.stream().collect(Collectors.joining(" ")).trim();
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
