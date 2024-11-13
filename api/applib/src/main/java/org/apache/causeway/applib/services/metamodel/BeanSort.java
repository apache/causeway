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
package org.apache.causeway.applib.services.metamodel;

import javax.imageio.spi.ServiceRegistry;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.services.factory.FactoryService;

/**
 * Top level object classification.
 *
 * @since 2.0 {@index}
 */
public enum BeanSort {
    /**
     * Stateful object, with a state that can be marshaled and unmarshaled.
     * <p>
     * Includes classes annotated with {@code @DomainObject}, when *not* associated
     * with a persistence layer. <p>  see also {@link #ENTITY}
     */
    VIEW_MODEL(new BeanPolicy(BeanPolicy.INTROSPECTION | BeanPolicy.WRAPPING | BeanPolicy.ELEMENTTYPE | BeanPolicy.NON_SUBSTITUTABLE)),
    /**
     * Persistable object, associated with a persistence layer/context.
     * <p>
     * Includes classes annotated with {@code @DomainObject}, when associated
     * with a persistence layer. <p>  see also {@link #VIEW_MODEL}
     *
     */
    ENTITY(new BeanPolicy(BeanPolicy.INTROSPECTION | BeanPolicy.WRAPPING | BeanPolicy.ELEMENTTYPE | BeanPolicy.NON_SUBSTITUTABLE)),
    /**
     * Injectable object, associated with a lifecycle context
     * (application-scoped, request-scoped, ...).
     * <p>
     * In other words: Indicates that the class is a Spring managed bean,
     * which IS also contributing to the UI or WEB API. Hence needs introspection.
     * <p>
     * The {@link ServiceRegistry} must be aware.
     * <p>
     * to be introspected: YES
     */
    MANAGED_BEAN_CONTRIBUTING(new BeanPolicy(BeanPolicy.INTROSPECTION | BeanPolicy.INJECTABLE | BeanPolicy.WRAPPING | BeanPolicy.NON_SUBSTITUTABLE)),
    /**
     * Injectable object, associated with a lifecycle context
     * (application-scoped, request-scoped, ...).
     * <p>
     * In other words: Indicates that the class is a Spring managed bean,
     * which is NOT contributing to the UI or WEB API. Hence not introspected.
     * <p>
     * The {@link ServiceRegistry} must be aware regardless.
     * <p>
     * This is also the fallback {@link BeanSort} for beans originating from {@link Bean} annotated factory methods,
     * when the bean class itself declares no annotations.
     * <p>
     * to be introspected: NO
     */
    MANAGED_BEAN_NOT_CONTRIBUTING(new BeanPolicy(BeanPolicy.INJECTABLE | BeanPolicy.WRAPPING | BeanPolicy.NON_SUBSTITUTABLE)),
    /**
     * Object associated with an <i>entity</i>, <i>viewmodel</i> or <i>domain-service</i>
     * to act as contributer of a single <i>domain-action</i> or
     * <i>domain-property</i> or <i>domain-collection</i>.
     */
    MIXIN(new BeanPolicy(BeanPolicy.INTROSPECTION | BeanPolicy.WRAPPING)),
    /**
     * Immutable, serializable object.
     * Values (including enums) my have object support methods, hence needs introspection.
     */
    VALUE(new BeanPolicy(BeanPolicy.INTROSPECTION | BeanPolicy.ELEMENTTYPE)),
    /**
     * Container of objects.
     */
    COLLECTION(new BeanPolicy(0)),
    /**
     * A non concrete type, that is a placeholder for a concrete implementer.
     * <p>
     * E.g. action return types or collection element types could be interfaces or abstract types
     * (as discovered by reflection during introspection)
     */
    ABSTRACT(new BeanPolicy(BeanPolicy.INTROSPECTION | BeanPolicy.ELEMENTTYPE)),
    /**
     * Type must NOT be added to the meta-model, eg. by means of
     * {@link org.apache.causeway.applib.annotation.Domain.Exclude} or {@link Programmatic}.
     * Consequently the specification-loader should skip introspection of those types.
     * <p>
     * In other words: Indicates that the class is some bean,
     * which is NOT contributing to the UI or WEB API, and
     * also NOT managed by Spring.
     * <p>
     * {@link ServiceRegistry} will NOT be aware.
     * <p>
     * {@link FactoryService#create(Class)} will nevertheless support
     * those programmatic beans. They may have injection points that need resolving.
     */
    PROGRAMMATIC(new BeanPolicy(BeanPolicy.WRAPPING)),
    /**
     * Type must not be added to the meta-model, eg. by means of
     * {@link javax.enterprise.inject.Vetoed} or {@link Profile}.
     * Consequently the specification-loader should skip introspection of those types.
     * If discovered by Spring during class-path scanning, we remove the corresponding {@link BeanDefinition}.
     * <p>
     * {@link ServiceRegistry} must not be aware of those types.
     * <p>
     * {@link FactoryService#getOrCreate(Class)} must fail for those types.
     */
    @SuppressWarnings("javadoc")
    VETOED(new BeanPolicy(0)),
    UNKNOWN(new BeanPolicy(BeanPolicy.ELEMENTTYPE));

    public record BeanPolicy(int flags) {
        final static int INTROSPECTION = 0x01;
        final static int INJECTABLE = 0x02;
        final static int WRAPPING = 0x04;
        final static int ELEMENTTYPE = 0x08;
        final static int NON_SUBSTITUTABLE = 0x10;
        /**
         * Enables meta-model introspection.
         * Potentially contributes to the UI or Web API.
         */
        public boolean isIntrospectionAllowed() { return (flags & INTROSPECTION) !=0; }
        /**
         * Contributes actions, members and/or object support to the UI or Web API.
         */
        public boolean contributesToUiOrWebApi() { return isIntrospectionAllowed(); } // seems to be just a synonym
        /**
         * Whether corresponding type is in principle injectable,
         * that is, provided it is also registered with a Spring context.
         */
        public boolean isInjectable() { return (flags & INJECTABLE) !=0; }
        public boolean isWrappingSupported() { return (flags & WRAPPING) !=0; }
        public boolean isAllowedAsMemberElementType() { return (flags & ELEMENTTYPE) !=0; }
        public boolean isNotSubstitutable() { return (flags & NON_SUBSTITUTABLE) !=0; }
    }

    // -- POLICY

    private final BeanPolicy policy;
    private BeanSort(final BeanPolicy policy) { this.policy = policy;}
    public BeanPolicy policy() { return policy; }

    // -- PREDICATES

    public boolean isManagedBeanContributing() { return this == MANAGED_BEAN_CONTRIBUTING; }
    public boolean isManagedBeanNotContributing() { return this == MANAGED_BEAN_NOT_CONTRIBUTING; }
    public boolean isMixin() { return this == MIXIN; }
    public boolean isViewModel() { return this == VIEW_MODEL; }
    public boolean isValue() { return this == VALUE; }
    public boolean isCollection() { return this == COLLECTION; }
    public boolean isEntity() { return this == ENTITY; }
    public boolean isAbstract() { return this == ABSTRACT; }
    public boolean isProgrammatic() { return this == PROGRAMMATIC; }
    public boolean isVetoed() { return this == VETOED; }
    public boolean isUnknown() { return this == UNKNOWN; }

    public boolean isManagedBeanAny() {
        return this == MANAGED_BEAN_CONTRIBUTING
                || this == MANAGED_BEAN_NOT_CONTRIBUTING;
    }

}
