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
package org.apache.isis.persistence.jdo.metamodel.facets.object.query;

import java.util.Map;

import javax.jdo.annotations.Query;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.persistence.jdo.provider.metamodel.facets.object.query.JdoNamedQuery;
import org.apache.isis.persistence.jdo.provider.metamodel.facets.object.query.JdoQueryFacet;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public class JdoQueryFacetAbstract
extends FacetAbstract
implements JdoQueryFacet {

    public static Class<? extends Facet> type() {
        return JdoQueryFacet.class;
    }

    @Getter(onMethod_ = {@Override})
    private final Can<JdoNamedQuery> namedQueries;

    public JdoQueryFacetAbstract(
            final @NonNull FacetHolder holder,
            final @NonNull Can<Query> jdoNamedQueries) {

        super(JdoQueryFacetAbstract.type(), holder);

        val objSpec = (ObjectSpecification) getFacetHolder();
        this.namedQueries = jdoNamedQueries.map(jdoNamedQuery->new JdoNamedQuery(jdoNamedQuery, objSpec));
    }

    @Override
    public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("namedQueries", namedQueries);
    }

}
