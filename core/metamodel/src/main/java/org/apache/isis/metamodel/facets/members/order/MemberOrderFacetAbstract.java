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

package org.apache.isis.metamodel.facets.members.order;

import java.util.Map;

import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FacetAbstract;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.IdentifiedHolder;

public abstract class MemberOrderFacetAbstract extends FacetAbstract implements MemberOrderFacet {


    public static Class<? extends Facet> type() {
        return MemberOrderFacet.class;
    }

    private final String originalName;
    private final String name;
    private final String sequence;

    public MemberOrderFacetAbstract(
            final String name,
            final String sequence,
            final TranslationService translationService,
            final FacetHolder holder) {
        this(translatedValueElse(name, "", translationService, holder),
                sequence,
                holder);
    }

    public MemberOrderFacetAbstract(
            final String name,
            final String sequence,
            final FacetHolder holder) {
        super(type(), holder);
        this.name = valueElse(name, "");
        this.originalName = valueElse(name, "");
        this.sequence = valueElse(sequence, "1");
    }

    private static String translatedValueElse(
            final String name,
            final String defaultValue,
            final TranslationService translationService,
            final FacetHolder holder) {
        final boolean nullOrEmpty = _Strings.isNullOrEmpty(name);
        if (nullOrEmpty) {
            return defaultValue;
        } else {
            final IdentifiedHolder identifiedHolder = (IdentifiedHolder) holder;
            final String context = identifiedHolder.getIdentifier().getClassName();
            return translationService.translate(context, name);
        }
    }

    private static String valueElse(final String name, final String defaultValue) {
        final boolean nullOrEmpty = _Strings.isNullOrEmpty(name);
        if (nullOrEmpty) {
            return defaultValue;
        } else {
            return name;
        }
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String untranslatedName() {
        return originalName;
    }

    @Override
    public String sequence() {
        return sequence;
    }

    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("name", name);
        attributeMap.put("originalName", originalName);
        attributeMap.put("sequence", sequence);
    }
}
