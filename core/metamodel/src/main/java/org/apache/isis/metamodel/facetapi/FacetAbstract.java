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

package org.apache.isis.metamodel.facetapi;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.isis.core.commons.internal.collections._Sets;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.metamodel.context.MetaModelContext;

import static org.apache.isis.core.commons.internal.base._With.requires;

import lombok.Setter;
import lombok.val;


public abstract class FacetAbstract implements Facet, MetaModelContext.Delegating {

    public enum Derivation {
        DERIVED,
        NOT_DERIVED
    }

    private Facet underlyingFacet;

    private final Class<? extends Facet> facetType;
    @Setter private Class<? extends Facet> facetAliasType;
    private Set<Facet> contributedFacets; // lazy init
    
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
            Class<? extends Facet> facetType,
            FacetHolder holder,
            Derivation derivation) {
        
        this.facetType = requires(facetType, "facetType"); 
        setFacetHolder(holder);
        this.derived = (derivation == Derivation.DERIVED);
    }
    
    protected FacetAbstract(
            Class<? extends Facet> facetType,
            FacetHolder holder) {
        
        this(facetType, holder, Derivation.NOT_DERIVED);
    }

    @Override
    public final Class<? extends Facet> facetType() {
        return facetType;
    }
    
    @Override
    public Class<? extends Facet> facetAliasType() {
        return facetAliasType!=facetType ? facetAliasType : null; // avoids facetAliasType equals facetType 
    }
    
    @Override
    public FacetHolder getFacetHolder() {
        return holder;
    }
    
    @Override
    public MetaModelContext getMetaModelContext() {
        return holder.getMetaModelContext();
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
//            if(underlyingFacet instanceof MultiTypedFacet) {
//                val multiTypedFacet = (MultiTypedFacet) underlyingFacet;
//                val matches = compatible(multiTypedFacet);
//                if(!matches) {
//                    throw new IllegalArgumentException("illegal argument, expected underlying facet (a multi-valued facet) to have equivalent to the facet type (or facet types) of this facet");
//                }
//            } else {
                
                val underlyingFacetType = underlyingFacet.facetType();
                if(!Objects.equals(underlyingFacetType, facetType)) {
                    val msg = String.format(
                            "type-missmatch: underlying facet's type '%s' "
                            + "must match this facet's type '%s'", 
                            underlyingFacetType, facetType);
                    throw _Exceptions.unrecoverable(msg);
                }
 
 //           }
        }
        this.underlyingFacet = underlyingFacet;
    }

//    private boolean compatible(final MultiTypedFacet multiTypedFacet) {
//
//        if (!(this instanceof MultiTypedFacet)) {
//            return multiTypedFacet.containsFacetTypeOf(this.facetType);
//        }
//
//        val thisAsMultiTyped = (MultiTypedFacet) this;
//        
//        return thisAsMultiTyped.facetTypes()
//                .anyMatch(multiTypedFacet::containsFacetTypeOf);
//    }

    /**
     * Assume implementation is <i>not</i> a no-op.
     *
     * <p>
     * No-op implementations should override and return <tt>true</tt>.
     */
    @Override
    public boolean isFallback() {
        return false;
    }

    /**
     * Default implementation of this method that returns <tt>true</tt>, ie
     * should replace (none {@link #isFallback() no-op} implementations.
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
        if(isFallback()) {
            attributeMap.put("noop", isFallback());
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

    // -- CONTRIBUTED FACET SUPPORT
    
    @Override
    public void addContributedFacet(Facet contributedFacet) {
        if(contributedFacets==null) {
            contributedFacets = _Sets.newHashSet();
        }
        contributedFacets.add(contributedFacet);
    }
    
    @Override
    public void forEachContributedFacet(Consumer<Facet> onContributedFacet) {
        if(contributedFacets!=null) {
            contributedFacets.forEach(onContributedFacet);
        }
    }


}
