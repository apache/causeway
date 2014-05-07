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

import org.apache.isis.applib.annotation.WrapperPolicy;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.applib.services.eventbus.PropertyChangedEvent;
import org.apache.isis.core.commons.lang.ObjectExtensions;
import org.apache.isis.core.metamodel.facetapi.DecoratingFacet;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.MultiTypedFacet;
import org.apache.isis.core.metamodel.facets.properties.modify.PropertyClearFacet;
import org.apache.isis.core.metamodel.facets.properties.modify.PropertySetterFacet;

public interface PostsEventWithWrapperPolicy {

    WrapperPolicy getWrapperPolicy();
    
    public static class Util {
        private Util(){}

        /**
         * Returns the provided {@link Facet facet} as an {@link PostsEventWithWrapperPolicy} if
         * it either is one or if it is a {@link DecoratingFacet} that in turn wraps
         * an {@link PostsEventWithWrapperPolicy}.
         * 
         * <p>
         * Otherwise, returns <tt>null</tt>.
         */
        public static PostsEventWithWrapperPolicy getWrapperPolicyFacet(final Facet facet) {
            if (facet instanceof PostsEventWithWrapperPolicy) {
                return (PostsEventWithWrapperPolicy) facet;
            }
            if (facet.getUnderlyingFacet() instanceof PostsEventWithWrapperPolicy) {
                return (PostsEventWithWrapperPolicy) facet.getUnderlyingFacet();
            }
            if (facet instanceof DecoratingFacet) {
                final DecoratingFacet<?> decoratingFacet = ObjectExtensions.asT(facet);
                return getWrapperPolicyFacet(decoratingFacet.getDecoratedFacet());
            }
            return null;
        }
    }
}
