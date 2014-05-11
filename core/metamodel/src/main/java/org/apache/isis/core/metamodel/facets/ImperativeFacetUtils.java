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

package org.apache.isis.core.metamodel.facets;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.isis.applib.filter.Filters;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;

public final class ImperativeFacetUtils {

    private ImperativeFacetUtils() {
    }

    /**
     * @deprecated - use {@link ImperativeFacet.Util#getImperativeFacet(Facet)}
     */
    @Deprecated
    public static ImperativeFacet getImperativeFacet(final Facet facet) {
        return ImperativeFacet.Util.getImperativeFacet(facet);
    }

    /**
     * @deprecated - use {@link ImperativeFacet.Util#isImperativeFacet(Facet)}
     */
    @Deprecated
    public static boolean isImperativeFacet(final Facet facet) {
        return ImperativeFacet.Util.isImperativeFacet(facet);
    }

    /**
     * @deprecated - use {@link ImperativeFacet.Util#getImperativeFacet(Facet)}
     */
    @Deprecated
    public static ImperativeFacet.Flags getImperativeFacetFlags(final ObjectMember member, final Method method) {
        return ImperativeFacet.Util.getFlags(member, method);
    }

}
