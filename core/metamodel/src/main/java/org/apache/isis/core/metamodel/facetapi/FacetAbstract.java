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

import static org.apache.isis.commons.internal.base._With.requires;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.isis.core.commons.ensure.Ensure;
import org.apache.isis.core.metamodel.MetaModelContext;


public abstract class FacetAbstract implements Facet, MetaModelContext.Delegating {

    public enum Derivation {
        DERIVED,
        NOT_DERIVED
    }

    private Facet underlyingFacet;

    private final Class<? extends Facet> facetType;
    private final boolean derived;
    private FacetHolder holder;

    /**
     * Populated in {@link #setFacetHolder(FacetHolder)} if the provided holder
     * implements {@link IdentifiedHolder}.
     *
     * <p>
     * Otherwise is <tt>null</tt>.
     */
    private IdentifiedHolder identifiedHolder;

    public FacetAbstract(
            final Class<? extends Facet> facetType,
            final FacetHolder holder,
            final Derivation derivation) {
        this.facetType = requires(facetType, "facetType"); 
        setFacetHolder(holder);
        this.derived = (derivation == Derivation.DERIVED);
    }

    @Override
    public final Class<? extends Facet> facetType() {
        return facetType;
    }

    @Override
    public FacetHolder getFacetHolder() {
        return holder;
    }

    @Override
    public boolean isDerived() {
        return derived;
    }

    /**
     * Convenience method that returns {@link #getFacetHolder()} downcast to
     * {@link IdentifiedHolder} if the implementation does indeed inherit from
     * {@link IdentifiedHolder}, otherwise <tt>null</tt>.
     */
    public IdentifiedHolder getIdentified() {
        return identifiedHolder;
    }

    @Override
    public Facet getUnderlyingFacet() {
        return underlyingFacet;
    }

    @Override
    public void setUnderlyingFacet(final Facet underlyingFacet) {
        if(underlyingFacet != null) {
            if(underlyingFacet instanceof MultiTypedFacet) {
                final MultiTypedFacet multiTypedFacet = (MultiTypedFacet) underlyingFacet;
                final boolean matches = compatible(multiTypedFacet);
                if(!matches) {
                    throw new IllegalArgumentException("illegal argument, expected underlying facet (a multi-valued facet) to have equivalent to the facet type (or facet types) of this facet");
                }
            } else {
                Ensure.ensureThatArg(
                        underlyingFacet.facetType(), 
                        type->Objects.equals(type, facetType), 
                        ()->String.format("type-missmatch: underlying facet's type '%s' must match this facet's type '%s'"));
            }
        }
        this.underlyingFacet = underlyingFacet;
    }

    private boolean compatible(final MultiTypedFacet multiTypedFacet) {

        if (!(this instanceof MultiTypedFacet)) {
            return multiTypedFacet.containsFacetTypeOf(this.facetType);
        }

        final MultiTypedFacet thisAsMultiTyped = (MultiTypedFacet) this;
        final Stream<Class<? extends Facet>> facetTypes = thisAsMultiTyped.facetTypes();
        return facetTypes
                .anyMatch(facetType->multiTypedFacet.containsFacetTypeOf(facetType));
    }

    /**
     * Assume implementation is <i>not</i> a no-op.
     *
     * <p>
     * No-op implementations should override and return <tt>true</tt>.
     */
    @Override
    public boolean isNoop() {
        return false;
    }

    /**
     * Default implementation of this method that returns <tt>true</tt>, ie
     * should replace (none {@link #isNoop() no-op} implementations.
     *
     * <p>
     * Implementations that don't wish to replace none no-op implementations
     * should override and return <tt>false</tt>.
     */
    @Override
    public boolean alwaysReplace() {
        return true;
    }

    @Override
    public void setFacetHolder(final FacetHolder facetHolder) {
        this.holder = facetHolder;
        this.identifiedHolder = (holder!=null && holder instanceof IdentifiedHolder) 
        		? (IdentifiedHolder) holder 
        				: null;
    }

    protected String toStringValues() {
        return "";
    }

    @Override
    public String toString() {
        String details = "";
        if (isValidating()) {
            details += "Validating";
        }
        if (isDisabling()) {
            details += (details.length() > 0 ? ";" : "") + "Disabling";
        }
        if (isHiding()) {
            details += (details.length() > 0 ? ";" : "") + "Hiding";
        }
        if (!"".equals(details)) {
            details = "interaction=" + details + ",";
        }

        final String className = getClass().getName();
        final String stringValues = toStringValues();
        if (getClass() != facetType()) {
            final String facetType = facetType().getName();
            details += "type=" + facetType.substring(facetType.lastIndexOf('.') + 1);
        }
        if (!"".equals(stringValues)) {
            details += ",";
        }
        return className.substring(className.lastIndexOf('.') + 1) + "[" + details + stringValues + "]";
    }

    private boolean isHiding() {
        return Hiding.class.isAssignableFrom(getClass());
    }

    private boolean isDisabling() {
        return Disabling.class.isAssignableFrom(getClass());
    }

    private boolean isValidating() {
        return Validating.class.isAssignableFrom(getClass());
    }

    @Override
    public void appendAttributesTo(final Map<String, Object> attributeMap) {
        if(derived) {
            attributeMap.put("derived", derived);
        }
        attributeMap.put("underlyingFacet", underlyingFacet);
        if(isNoop()) {
            attributeMap.put("noop", isNoop());
        }
        if(isHiding()) {
            attributeMap.put("hiding", isHiding());
        }
        if(isDisabling()) {
            attributeMap.put("disabling", isDisabling());
        }
        if(isValidating()) {
            attributeMap.put("validating", isValidating());
        }
    }

    /**
     * Marker interface used within {@link #toString()}.
     */
    public static interface Hiding {
    }

    /**
     * Marker interface used within {@link #toString()}.
     */
    public static interface Disabling {
    }

    /**
     * Marker interface used within {@link #toString()}.
     */
    public static interface Validating {
    }
    
    // -- dependencies
    
    @Override
    public MetaModelContext getMetaModelContext() {
        return MetaModelContext.current();
    }
   

}
