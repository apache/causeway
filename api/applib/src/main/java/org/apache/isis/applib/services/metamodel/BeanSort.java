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
package org.apache.isis.applib.services.metamodel;

import javax.enterprise.inject.Vetoed;

import org.springframework.context.annotation.Profile;

import org.apache.isis.applib.annotations.Programmatic;

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
    VIEW_MODEL,
    /**
     * Persistable object, associated with a persistence layer/context.
     * <p>
     * Includes classes annotated with {@code @DomainObject}, when associated
     * with a persistence layer. <p>  see also {@link #VIEW_MODEL}
     *
     */
    ENTITY,
    /**
     * Injectable object, associated with a lifecycle context
     * (application-scoped, request-scoped, ...).
     * <p>
     * to be introspected: YES
     */
    MANAGED_BEAN_CONTRIBUTING,
    /**
     * Injectable object, associated with a lifecycle context
     * (application-scoped, request-scoped, ...).
     * <p>
     * to be introspected: NO
     */
    MANAGED_BEAN_NOT_CONTRIBUTING,
    /**
     * Object associated with an 'entity' or 'bean' to act as contributer of
     * domain actions or properties. Might also be stateful similar to VIEW_MODEL.
     */
    MIXIN,
    /**
     * Immutable, serializable object.
     */
    VALUE,
    /**
     * Container of objects.
     */
    COLLECTION,
    /**
     * A non concrete type, that is a placeholder for a its concrete implementer.
     */
    ABSTRACT,
    /**
     * Type must not be added to the meta-model, eg. by means of
     * {@link Vetoed}, {@link Profile} or {@link Programmatic}
     */
    VETOED,
    UNKNOWN;

    // -- SIMPLE PREDICATES

    public boolean isManagedBeanContributing() {
        return this == MANAGED_BEAN_CONTRIBUTING;
    }

    public boolean isManagedBeanNotContributing() {
        return this == MANAGED_BEAN_NOT_CONTRIBUTING;
    }

    public boolean isManagedBean() {
        return this == MANAGED_BEAN_CONTRIBUTING
                || this == MANAGED_BEAN_NOT_CONTRIBUTING;
    }

    public boolean isMixin() {
        return this == MIXIN;
    }

    public boolean isViewModel() {
        return this == VIEW_MODEL;
    }

    public boolean isValue() {
        return this == VALUE;
    }

    public boolean isCollection() {
        return this == COLLECTION;
    }

    public boolean isEntity() {
        return this == ENTITY;
    }

    public boolean isAbstract() {
        return this == ABSTRACT;
    }

    public boolean isVetoed() {
        return this == VETOED;
    }

    public boolean isUnknown() {
        return this == UNKNOWN;
    }

    // -- HIGER LEVEL PREDICATES

    public boolean isToBeIntrospected() {

        if(isVetoed()
                || isUnknown()) {
            return false;
        }
        if(this == MANAGED_BEAN_NOT_CONTRIBUTING) {
            return false;
        }

        return true;
    }

    public boolean isWrappingSupported() {
        return isMixin() || isViewModel() || isEntity() || isManagedBean();
    }


    // ...
}
