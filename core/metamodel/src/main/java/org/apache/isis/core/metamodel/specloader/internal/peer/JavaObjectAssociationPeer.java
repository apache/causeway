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

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.metamodel.runtimecontext.spec.feature.MemberType;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.util.NameUtils;


public class JavaObjectAssociationPeer extends JavaObjectMemberPeer {

    public static JavaObjectAssociationPeer createPropertyPeer(Class<?> type, Method method, Class<?> returnType,
        SpecificationLoader specificationLoader) {
        return new JavaObjectAssociationPeer(MemberType.PROPERTY, type, method, returnType, specificationLoader);
    }
    
    public static JavaObjectAssociationPeer createCollectionPeer(Class<?> type, Method method,
        SpecificationLoader specificationLoader) {
        return new JavaObjectAssociationPeer(MemberType.COLLECTION, type, method, null, specificationLoader);
    }


    public JavaObjectAssociationPeer(final MemberType memberType, final Class<?> type, final Method method, final Class<?> returnType, SpecificationLoader specificationLoader) {
        super(memberType, type, method, determineIdentifier(type, method), returnType, specificationLoader);
    }

    private static Identifier determineIdentifier(Class<?> type, Method method) {
        final String capitalizedName = NameUtils.javaBaseName(method.getName());
        final String beanName = Introspector.decapitalize(capitalizedName);
        return Identifier.propertyOrCollectionIdentifier(type.getName(), beanName);
    }

    @Override
    public String toString() {
        return getMemberType().name() + " Peer [identifier=\"" + getIdentifier() + "\",type=" + getType().getName() + " ]";
    }



}
