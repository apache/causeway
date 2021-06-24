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
package org.apache.isis.core.metamodel.facets.members.described.method;

import java.lang.reflect.Method;

import org.apache.isis.commons.internal.base._Either;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.all.described.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.all.i8n.HasTranslation;
import org.apache.isis.core.metamodel.facets.all.i8n.imperative.HasImperativeText;
import org.apache.isis.core.metamodel.facets.all.i8n.imperative.I8nImperativeFacetAbstract;

import lombok.Getter;

public class DescribedAsFacetForMemberViaMethod
extends I8nImperativeFacetAbstract
implements
    DescribedAsFacet {

    private static final Class<? extends Facet> type() {
        return DescribedAsFacet.class;
    }

    @Getter(onMethod_ = {@Override})
    private final _Either<HasTranslation, HasImperativeText> specialization = _Either.right(this);

    public DescribedAsFacetForMemberViaMethod(
            final Method describedMethod,
            final FacetHolder holder) {
        super(type(), describedMethod, holder);
    }

}
