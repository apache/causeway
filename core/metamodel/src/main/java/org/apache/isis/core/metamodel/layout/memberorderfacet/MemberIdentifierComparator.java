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

package org.apache.isis.core.metamodel.layout.memberorderfacet;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.isis.applib.id.Identifier;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facets.FacetedMethod;

/**
 * Compares {@link FacetedMethod}) by {@link FacetedMethod#getIdentifier()}
 *
 */
public class MemberIdentifierComparator implements Comparator<IdentifiedHolder>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(final IdentifiedHolder o1, final IdentifiedHolder o2) {
        final Identifier identifier1 = o1.getIdentifier();
        final Identifier identifier2 = o2.getIdentifier();
        return identifier1.compareTo(identifier2);
    }

}
