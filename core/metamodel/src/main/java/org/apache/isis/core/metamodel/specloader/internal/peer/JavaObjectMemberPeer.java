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


package org.apache.isis.core.metamodel.specloader.internal.peer;

import java.lang.reflect.Method;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.filters.Filter;
import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.facets.FacetHolderImpl;
import org.apache.isis.core.metamodel.facets.MultiTypedFacet;
import org.apache.isis.core.metamodel.runtimecontext.spec.feature.MemberType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;


public abstract class JavaObjectMemberPeer implements FacetHolder,ObjectMemberPeer {

    private final FacetHolderImpl holder = new FacetHolderImpl();
    
    private final MemberType memberType;
    private final Class<?> owningType;
    private final Method method;
    private final Identifier identifier;
    private final SpecificationLoader specificationLoader;
    private Class<?> type;

    public JavaObjectMemberPeer(final MemberType memberType, final Class<?> owningType, final Method method, final Identifier identifier, Class<?> type, SpecificationLoader specificationLoader) {
        this.memberType = memberType;
        this.owningType = owningType;
        this.method = method;
        this.identifier = identifier;
        this.setType(type);
        this.specificationLoader = specificationLoader;
    }

    protected MemberType getMemberType() {
        return memberType;
    }

    
    /**
     * @return the method
     */
    public Method getMethod() {
        return method;
    }

    @Override
    public Identifier getIdentifier() {
        return identifier;
    }

    
    //////////////////////////////////////////////////////////////////////
    // Specification (derived from type)
    //////////////////////////////////////////////////////////////////////

    /**
     * return the object type, as a Class object, that the method returns.
     */
    @Override
    public ObjectSpecification getSpecification(final SpecificationLoader specificationLoader) {
        return getType() == null ? null : specificationLoader.loadSpecification(getType());
    }


    //////////////////////////////////////////////////////////////////////
    // type
    //////////////////////////////////////////////////////////////////////

    /**
     * Required for associations because this may not be known from the
     * accessor alone (might return a raw type such as <tt>java.util.List</tt>,
     * rather than a generic one such as <tt>java.util.List&lt;Customer&gt;</tt>).
     */
    public void setType(final Class<?> type) {
        this.type = type;
    }
    


    //////////////////////////////////////////////////////////////////////
    // facets
    //////////////////////////////////////////////////////////////////////

    @Override
    public Class<? extends Facet>[] getFacetTypes() {
        return holder.getFacetTypes();
    }

    @Override
    public boolean containsFacet(Class<? extends Facet> facetType) {
        return holder.containsFacet(facetType);
    }

    @Override
    public <T extends Facet> T getFacet(Class<T> cls) {
        return holder.getFacet(cls);
    }

    @Override
    public Facet[] getFacets(Filter<Facet> filter) {
        return holder.getFacets(filter);
    }

    @Override
    public void addFacet(Facet facet) {
        holder.addFacet(facet);
    }

    @Override
    public void addFacet(MultiTypedFacet facet) {
        holder.addFacet(facet);
    }

    @Override
    public void removeFacet(Facet facet) {
        holder.removeFacet(facet);
    }

    @Override
    public void removeFacet(Class<? extends Facet> facetType) {
        holder.removeFacet(facetType);
    }


    //////////////////////////////////////////////////////////////////////
    // toString, debug
    //////////////////////////////////////////////////////////////////////

    @Override
    public void debugData(final DebugString debug) {
    // debug.appendln("Identifier", identifier.toString());
    }


    //////////////////////////////////////////////////////////////////////
    // Dependencies
    //////////////////////////////////////////////////////////////////////
    
    protected SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }

    /**
     * @return the type
     */
    public Class<?> getType() {
        return type;
    }

    @Override
    public boolean isProperty() {
        return getMemberType().isProperty();
    }

    @Override
    public boolean isCollection() {
        return getMemberType().isCollection();
    }

    @Override
    public boolean isAction() {
        return getMemberType().isAction();
    }


}

