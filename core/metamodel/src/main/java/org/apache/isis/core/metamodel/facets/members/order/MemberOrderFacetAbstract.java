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

package org.apache.isis.core.metamodel.facets.members.order;

import com.google.common.base.Strings;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.MultipleValueFacetAbstract;
import org.apache.isis.core.metamodel.facets.members.order.MemberOrderFacet;

public abstract class MemberOrderFacetAbstract extends MultipleValueFacetAbstract implements MemberOrderFacet {

    public static Class<? extends Facet> type() {
        return MemberOrderFacet.class;
    }

    private final String name;
    private final String sequence;

    public MemberOrderFacetAbstract(final String name, final String sequence, final FacetHolder holder) {
        super(type(), holder);
        this.name = valueElse(name, "");
        this.sequence = valueElse(sequence, "1");
    }

    private static String valueElse(final String name, final String string) {
        return !Strings.isNullOrEmpty(name) ? name : string;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String sequence() {
        return sequence;
    }

}
