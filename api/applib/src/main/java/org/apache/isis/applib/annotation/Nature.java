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
package org.apache.isis.applib.annotation;

import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.cglib.proxy.Mixin;

import org.apache.isis.applib.ViewModel;

/**
 * The different sorts of domain objects recognized by Isis.
 *
 * @since 1.x {@index}
 */
public enum Nature {

    /**
     * The default; allows the programmer to combine <tt>@DomainObject</tt> annotation with the
     * {@link ViewModel} annotation, or the {@link XmlRootElement} annotation, or by implementing the
     * {@link org.apache.isis.applib.ViewModel} interface.
     */
    NOT_SPECIFIED,

    /**
     * A domain entity whose persistence is managed internally by Isis, 
     * using JPA or JDO as the persistence implementation.
     * <p>
     * Domain entities are considered to be part of the domain model layer.
     * <p>
     * Currently implies no additional semantics other than documentation.
     */
    ENTITY,

    /**
     * An object that is conceptually part of the application layer, and which surfaces behavior and/or state that
     * is aggregate of one or more domain entity.
     *
     * <p>
     * The identity of a view model is determined solely by the state of object's properties.
     * Using this nature should be considered exactly equivalent to annotating with {@link ViewModel}.
     *     
     * <p>
     * Note that collections are ignored; if their state is required to fully identify the view model, define the
     * view model using the JAXB {@link XmlRootElement} annotation instead (where the object's state is serialized
     * to an arbitrarily deep graph of data, with references to persistent entities transparently resolved to
     * <code>&lt;oid-dto&gt;</code> elements).
     *
     * @see ViewModel
     */
    VIEW_MODEL,

    /**
     * An object that acts as a mix-in to some other object, contributing behavior and/or derived state based on the
     * domain object.
     *
     * @see Mixin
     */
    MIXIN,

    /**
     * An object that is entirely managed by the underlying IoC container.
     * @apiNote EXPERIMENTAL
     */
    BEAN,
    ;
    
    public boolean isEntity() {
        return this == Nature.ENTITY;
    }
    
    
}
//end::refguide[]
