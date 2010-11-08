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

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;


public abstract class JavaObjectAssociationPeer extends JavaObjectMemberPeer implements ObjectAssociationPeer {

    private final boolean oneToMany;
    private final SpecificationLoader specificationLoader;
    protected Class<?> type;

    public JavaObjectAssociationPeer(final Identifier identifier, final Class<?> returnType, final boolean oneToMany, SpecificationLoader specificationLoader) {
        super(identifier);
        type = returnType;
        this.oneToMany = oneToMany;
        this.specificationLoader = specificationLoader;
    }

    /**
     * return the object type, as a Class object, that the method returns.
     */
    public ObjectSpecification getSpecification() {
        return type == null ? null : getSpecificationLoader().loadSpecification(type);
    }

    public void setType(final Class<?> type) {
        this.type = type;
    }

    public final boolean isOneToMany() {
        return oneToMany;
    }

    public final boolean isOneToOne() {
        return !isOneToMany();
    }

    //////////////////////////////////////////////////////////////////////
    // Dependencies
    //////////////////////////////////////////////////////////////////////
    
    protected SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }


}
