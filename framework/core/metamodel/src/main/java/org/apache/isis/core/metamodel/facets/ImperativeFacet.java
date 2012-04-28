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

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;

/**
 * A {@link Facet} implementation that ultimately wraps a {@link Method} or
 * possibly several equivalent methods, for a Java implementation of a
 * {@link ObjectMember}.
 * 
 * <p>
 * Used by <tt>ObjectSpecificationDefault#getMember(Method)</tt> in order to
 * reverse lookup {@link ObjectMember}s from underlying {@link Method}s. So, for
 * example, the facets that represents an action xxx, or an <tt>validateXxx</tt>
 * method, or an <tt>addToXxx</tt> collection, can all be used to lookup the
 * member.
 * 
 * <p>
 * Note that {@link Facet}s relating to the class itself (ie for
 * {@link ObjectSpecification}) should not implement this interface.
 */
public interface ImperativeFacet {

    /**
     * The {@link Method}s invoked by this {@link Facet}.
     * 
     * <p>
     * In the vast majority of cases there is only a single {@link Method} (eg
     * wrapping a property's getter). However, some {@link Facet}s, such as
     * those for callbacks, could map to multiple {@link Method}s.
     * Implementations that will return multiple {@link Method}s should
     * implement the {@link ImperativeFacetMulti} sub-interface that provides
     * the ability to {@link ImperativeFacetMulti#addMethod(Method) add}
     * {@link Method}s as part of the interface API. For example:
     * 
     * <pre>
     * if (someFacet instanceof ImperativeFacetMulti) {
     *     ImperativeFacetMulti ifm = (ImperativeFacetMulti)someFacet;
     *     ifm.addMethod(...);
     * }
     * </pre>
     */
    public List<Method> getMethods();

    /**
     * For use by
     * {@link FacetHolder#getFacets(org.apache.isis.core.metamodel.facetapi.progmodel.facets.org.apache.isis.nof.arch.facets.Facet.Filter)}
     */
    public static Filter<Facet> FILTER = new Filter<Facet>() {
        @Override
        public boolean accept(final Facet facet) {
            return ImperativeFacetUtils.isImperativeFacet(facet);
        }
    };

    /**
     * Whether invoking this requires a
     * {@link DomainObjectContainer#resolve(Object)} to occur first.
     */
    public boolean impliesResolve();

    /**
     * Whether invoking this method requires an
     * {@link DomainObjectContainer#objectChanged(Object)} to occur afterwards.
     * 
     * @return
     */
    public boolean impliesObjectChanged();

}
