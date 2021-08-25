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

package org.apache.isis.core.metamodel.facets.object.choices.enums;

import javax.inject.Inject;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueFacetUsingSemanticsProviderFactory;

import lombok.val;

public class EnumFacetUsingValueFacetUsingSemanticsProviderFactory
extends ValueFacetUsingSemanticsProviderFactory<Enum<?>> {

    @Inject
    public EnumFacetUsingValueFacetUsingSemanticsProviderFactory(final MetaModelContext mmc) {
        super(mmc);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        val cls = processClassContext.getCls();
        val facetHolder = processClassContext.getFacetHolder();

        if (!cls.isEnum()) {
            return;
        }

        addFacets(_Casts.uncheckedCast(
                new EnumValueSemanticsProvider<>(
                        processClassContext.getMemberIntrospectionPolicy(),
                        facetHolder,
                        _Casts.uncheckedCast(cls))));
        addFacet(new ChoicesFacetEnum(facetHolder, cls));
    }

}
