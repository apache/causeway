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

import java.beans.Introspector;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.metamodel.runtimecontext.spec.feature.FeatureType;
import org.apache.isis.core.metamodel.spec.identifier.Util;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.util.NameUtils;


public class ObjectMemberPeerImpl extends TypedHolderImpl implements ObjectMemberPeer {

    ////////////////////////////////////////////////////
    // Factory methods
    ////////////////////////////////////////////////////

    public static ObjectMemberPeer createCollectionPeer(Class<?> type, Method method,
        SpecificationLoader specificationLoader) {
        return new ObjectMemberPeerImpl(FeatureType.COLLECTION, type, method, propertyOrCollectionIdentifierFor(type, method), null, emptyListOfTypedHolder(), specificationLoader);
    }

    public static ObjectMemberPeer createActionPeer(Class<?> type, Method method, Class<?> returnType,
        SpecificationLoader specificationLoader) {
        return new ObjectMemberPeerImpl(FeatureType.ACTION, type, method, actionIdentifierFor(type, method), returnType, Util.getParamPeers(method), specificationLoader);
    }


    private static Identifier propertyOrCollectionIdentifierFor(Class<?> type, Method method) {
        final String capitalizedName = NameUtils.javaBaseName(method.getName());
        final String beanName = Introspector.decapitalize(capitalizedName);
        return Identifier.propertyOrCollectionIdentifier(type.getName(), beanName);
    }
    
    private static Identifier actionIdentifierFor(Class<?> type, Method method) {
        final String fullMethodName = method.getName();
        final Class<?>[] parameterTypes = method.getParameterTypes();
        return Identifier.actionIdentifier(type.getName(), fullMethodName, parameterTypes);
    }

    ////////////////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////////////////

    private final Class<?> owningType;
    private final Method method;
    private final Identifier identifier;
    private final SpecificationLoader specificationLoader;
    private final List<TypedHolder> children;

    @Override
    public List<TypedHolder> getChildren() {
        return children;
    }

    public static List<TypedHolder> emptyListOfTypedHolder() {
        return Collections.unmodifiableList(new ArrayList<TypedHolder>());
    }

    public static ObjectMemberPeer createPropertyPeer(Class<?> type, Method method, Class<?> returnType,
        SpecificationLoader specificationLoader) {
        return new ObjectMemberPeerImpl(FeatureType.PROPERTY, type, method, propertyOrCollectionIdentifierFor(type, method), returnType, emptyListOfTypedHolder(), specificationLoader);
    }


    public ObjectMemberPeerImpl(final FeatureType featureType, final Class<?> owningType, final Method method, final Identifier identifier, Class<?> type, final List<TypedHolder> children, SpecificationLoader specificationLoader) {
        super(featureType, type);
        this.owningType = owningType;
        this.method = method;
        this.identifier = identifier;
        this.children = children;
        this.specificationLoader = specificationLoader;
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
    // toString, debug
    //////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return getFeatureType().name() + " Peer [identifier=\"" + getIdentifier() + "\",type=" + getType().getName() + " ]";
    }

    @Override
    public void debugData(final DebugString debug) {
        // TODO: reinstate
        // debug.appendln("Identifier", identifier.toString());
    }


    //////////////////////////////////////////////////////////////////////
    // Dependencies
    //////////////////////////////////////////////////////////////////////
    
    protected SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }

}

