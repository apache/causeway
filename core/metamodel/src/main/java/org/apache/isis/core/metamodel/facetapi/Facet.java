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
package org.apache.isis.core.metamodel.facetapi;

import java.util.Optional;

import org.apache.isis.applib.Identifier;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.isis.core.metamodel.facets.all.named.ObjectNamedFacet;

public interface Facet
extends
    FacetWithAttributes,
    HasSemanticEquality {

    /**
     * @implSpec Ordinal dictates precedence. A higher ordinal
     * (corresponding to the ascending order of appearance) has
     * higher precedence.
     */
    public static enum Precedence {

        /**
         * Whether this facet implementation is a fallback. Meaning it is treated
         * with lowest priority, always overruled by any other facet of same type.
         */
        FALLBACK,

        /**
         * Whether this facet implementation is inferred (as opposed to explicit);
         * <p>
         * For example, we might derive the typical length of a property based on
         * its type; but if the typical length has been explicitly specified using
         * an annotation then that should take precedence.
         */
        INFERRED,

        /**
         * Lower precedence than {@link #DEFAULT}. In other words, is overruled by {@link #DEFAULT}.
         */
        LOW,

        /**
         * The default as used with {@link FacetAbstract}, if not specified otherwise.
         */
        DEFAULT,

        /**
         * Higher precedence than {@link #DEFAULT}. In other words, overrules {@link #DEFAULT}.
         */
        HIGH,

        /**
         * Higher precedence than {@link #HIGH}. In other words, overrules {@link #HIGH}.
         */
        IMPERATIVE,

        /**
         * Higher precedence than {@link #IMPERATIVE}. In other words, overrules {@link #IMPERATIVE}.
         * <p>
         * Reserved for facet post-processing, when synthesizing a 'virtual' facet
         * from facets of lower rank. (eg. {@link ObjectNamedFacet} with its singular and plural noun-form support)
         */
        SYNTHESIZED,

        /**
         * Highest precedence, with special behavior and restrictions:
         * <ul>
         * <li>
         *      There can by only one {@link Facet} with {@link Precedence#EVENT}
         *      per {@link FacetHolder} and facet-type.
         * </li>
         * <li>
         *      If there are no subscribers to the event or subscribers are indifferent,
         *      then the 'winning' facet (if any) from the next lower {@link Precedence} rank,
         *      is used instead.
         * </li>
         * </ul>
         */
        EVENT,
        ;

        public boolean isFallback() {
            return this == FALLBACK;
        }

        public boolean isInferred() {
            return this == INFERRED;
        }

        public boolean isEvent() {
            return this == EVENT;
        }

    }

    /**
     * The {@link FacetHolder holder} of this facet.
     */
    FacetHolder getFacetHolder();

    /**
     * Identifier of the feature this facet is associated with.
     * @see FacetHolder#getFeatureIdentifier()
     */
    default Identifier getFeatureIdentifier() {
        return getFacetHolder().getFeatureIdentifier();
    }

    default Optional<FacetRanking> getSharedFacetRanking() {
        return getFacetHolder().getFacetRanking(facetType());
    }

    default FacetRanking getSharedFacetRankingElseFail() {
        return getSharedFacetRanking()
                .orElseThrow(()->_Exceptions
                        .noSuchElement("no ranking found with facet holder for facetType %s", facetType()));
    }

    /**
     * Whether to collect all facets of this type into the facet ranks,
     * or (when {@code false}) allow for (heap) optimization,
     * such that only (current) top ranks are populated.
     * @since 2.0
     */
    default boolean isPopulateAllFacetRanks() {
        return false;
    }

    /**
     * Determines the type of this facet to be stored under.
     *
     * <p>
     * The framework looks for {@link Facet}s of certain well-known facet types.
     * Each facet implementation must specify which type of facet it corresponds
     * to. This therefore allows the (rules of the) programming model to be
     * varied without impacting the rest of the framework.
     *
     * <p>
     * For example, the <tt>ActionInvocationFacet</tt> specifies the facet to
     * invoke an action. The typical implementation of this wraps a
     * <tt>public</tt> method. However, a different facet factory could be
     * installed that creates facet also of type {@link ActionInvocationFacet}
     * but that have some other rule, such as requiring an <i>action</i> prefix,
     * or that decorate the interaction by logging it, for example.
     */
    Class<? extends Facet> facetType();

    /**
     * Facets with higher precedence override facets with lower precedence.
     * On same precedence, its unspecified, which one wins. (Warnings should be logged.)
     */
    public Precedence getPrecedence();

}
