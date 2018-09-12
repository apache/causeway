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
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.commons.lang.ObjectExtensions;
import org.apache.isis.core.metamodel.facetapi.DecoratingFacet;
import org.apache.isis.core.metamodel.facetapi.Facet;
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
public interface ImperativeFacet extends Facet {

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

    public static enum Intent {
        CHECK_IF_HIDDEN,
        CHECK_IF_DISABLED,
        CHECK_IF_VALID,
        ACCESSOR,
        EXECUTE,
        MODIFY_PROPERTY,
        /**
         * Modify property using modify/clear rather than simply using set.
         */
        MODIFY_PROPERTY_SUPPORTING,
        MODIFY_COLLECTION_ADD,
        MODIFY_COLLECTION_REMOVE,
        CHOICES_OR_AUTOCOMPLETE,
        DEFAULTS,
        INITIALIZATION,
        LIFECYCLE,
        UI_HINT
    }

    /**
     * The intent of this method, so that the {@link WrapperFactory} knows whether to delegate on or to reject.
     * @param method - one of the methods returned from {@link #getMethods()}
     */
    public Intent getIntent(Method method);


    public static Predicate<Facet> PREDICATE = ImperativeFacet.Util::isImperativeFacet;

    // //////////////////////////////////////

    public static class Util {
        private Util(){}

        /**
         * Returns the provided {@link Facet facet} as an {@link ImperativeFacet} if
         * it either is one or if it is a {@link DecoratingFacet} that in turn wraps
         * an {@link ImperativeFacet}.
         *
         * <p>
         * Otherwise, returns <tt>null</tt>.
         */
        public static ImperativeFacet getImperativeFacet(final Facet facet) {
            if (facet instanceof ImperativeFacet) {
                return (ImperativeFacet) facet;
            }
            if (facet.getUnderlyingFacet() instanceof ImperativeFacet) {
                return (ImperativeFacet) facet.getUnderlyingFacet();
            }
            if (facet instanceof DecoratingFacet) {
                final DecoratingFacet<?> decoratingFacet = ObjectExtensions.asT(facet);
                return getImperativeFacet(decoratingFacet.getDecoratedFacet());
            }
            return null;
        }

        public static boolean isImperativeFacet(final Facet facet) {
            return getImperativeFacet(facet) != null;
        }

        public static Intent getIntent(final ObjectMember member, final Method method) {
            final List<ImperativeFacet> imperativeFacets = _Lists.newArrayList();
            final Stream<Facet> allFacets = member.streamFacets();
            allFacets.forEach(facet->{
                final ImperativeFacet imperativeFacet = ImperativeFacet.Util.getImperativeFacet(facet);
                if (imperativeFacet == null) {
                    return;
                }
                final List<Method> methods = imperativeFacet.getMethods();
                if (!methods.contains(method)) {
                    return;
                }
                imperativeFacets.add(imperativeFacet);
            });
            
            switch(imperativeFacets.size()) {
            case 0:
                break;
            case 1:
                return imperativeFacets.get(0).getIntent(method);
            default:
                Intent intentToReturn = null;
                for (ImperativeFacet imperativeFacet : imperativeFacets) {
                    Intent intent = imperativeFacet.getIntent(method);
                    if(intentToReturn == null) {
                        intentToReturn = intent;
                    } else if(intentToReturn != intent) {
                        throw new IllegalArgumentException(member.getIdentifier().toClassAndNameIdentityString() +  ": more than one ImperativeFacet for method " + method.getName() + " , with inconsistent intents: " + imperativeFacets.toString());
                    }
                }
                return intentToReturn;
            }
            throw new IllegalArgumentException(member.getIdentifier().toClassAndNameIdentityString() +  ": unable to determine intent of " + method.getName());
        }
    }

}
