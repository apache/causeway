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
package org.apache.causeway.applib.annotation;

import jakarta.xml.bind.annotation.XmlRootElement;

import org.springframework.cglib.proxy.Mixin;

import org.apache.causeway.applib.ViewModel;

/**
 * The different sorts of domain objects recognized by Causeway.
 *
 * @since 1.x {@index}
 */
public enum Nature {

    /**
     * The default; allows the programmer to combine <tt>@DomainObject</tt> annotation with the
     * {@link ViewModel} annotation, or the {@link XmlRootElement} annotation, or by implementing the
     * {@link org.apache.causeway.applib.ViewModel} interface.
     */
    NOT_SPECIFIED,

    /**
     * A domain entity whose persistence is managed by Causeway using the configured object store,
     * eg JPA/EclipseLink.
     *
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
     *
     * <p>
     *     Some possible use cases for this are:
     *
     *     <ul>
     *         <li>
     *             <p>
     *                 As a helper service that is used to emit messages through {@link Action#executionPublishing()}.
     *             </p>
     *             <p>
     *                 The service itself isn't rendered anywhere, but its actions can be invoked through the {@link org.apache.causeway.applib.services.wrapper.WrapperFactory}.
     *                 (Or as a variant, it might expose a {@link Programmatic} API and then delegate to its own action via the {@link org.apache.causeway.applib.services.wrapper.WrapperFactory}.
     *             </p>
     *         </li>
     *         <li>
     *             <p>
     *                 As a service representing a facade to a module, so that code in another (untrusted) module can only execute through {@link Action}s
     *             </p>
     *             <p>
     *                 Again, either the calling module is expected to use the {@link org.apache.causeway.applib.services.wrapper.WrapperFactory} when invoking
     *                 the facade service, or - since the calling code is treated untrusted - then the same self-delegation approach as for the previous example could be used,
     *                 whereby the facade service exposes a {@link Programmatic} API and then delegates to its own action via the {@link org.apache.causeway.applib.services.wrapper.WrapperFactory}.
     *             </p>
     *         </li>
     *     </ul>
     * </p>
     *
     *
     * <p>
     *     <b>IMPORTANT</b> the class must <i>also</i> be annotated with an appropriate
     *     {@link org.springframework.context.annotation.Scope}, eg <code>@Scope(&quot;singleton&quot;)</code> or <code>@Scope(&quot;prototype&quot;)</code>
     * </p>
     *
     * @apiNote EXPERIMENTAL
     */
    BEAN,
    ;

    public boolean isNotSpecified() {
        return this == Nature.NOT_SPECIFIED;
    }

    public boolean isEntity() {
        return this == Nature.ENTITY;
    }

    public boolean isMixin() {
        return this == Nature.MIXIN;
    }

    public boolean isViewModel() {
        return this == Nature.VIEW_MODEL;
    }

    public boolean isBean() {
        return this == Nature.BEAN;
    }

}
