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

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Domain semantics for domain objects (entities and view models; for services see {@link org.apache.isis.applib.annotation.DomainService}).
 */
@Inherited
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface DomainObject {

    /**
     * Whether the entity should be audited (note: does not apply to view models or other recreatable objects.
     *
     * <p>
     * Requires that an implementation of the {@link org.apache.isis.applib.services.audit.AuditingService3} is
     * registered with the framework.
     * </p>
     */
    Auditing auditing() default Auditing.AS_CONFIGURED;


    // //////////////////////////////////////


    /**
     * Whether changes to the object should be published.
     *
     * <p>
     * Requires that an implementation of the {@link org.apache.isis.applib.services.publish.PublishingService} is
     * registered with the framework.
     * </p>
     */
    Publishing publishing() default Publishing.AS_CONFIGURED;

    /**
     * The factory to construct the payload factory.
     *
     * <p>
     *     If not specified then a default implementation will be used.
     * </p>
     *
     * <p>
     *     TODO: ??? factor out PayloadFactory.Default so similar to design similar to @Action(domainEvent=...)
     * </p>
     */
    Class<? extends PublishingPayloadFactoryForObject> publishingPayloadFactory() default PublishingPayloadFactoryForObject.class;


    // //////////////////////////////////////


    /**
     * The class of the domain service that provides an <code>autoComplete(String)</code> method.
     *
     * <p>
     * It is sufficient to specify an interface rather than a concrete type.
     */
    Class<?> autoCompleteRepository() default Object.class;


    /**
     * The action to use in order to perform the auto-complete search
     * (defaults to &quot;autoComplete&quot;).
     *
     * <p>
     * The action is required to accept a single string parameter, and must return
     */
    String autoCompleteAction() default "autoComplete";


    // //////////////////////////////////////


    /**
     * Indicates that the class has a bounded, or finite, set of instances.
     * 
     * <p>
     *     Takes precedence over auto-complete.
     * </p>
     */
    boolean bounded() default false;


    // //////////////////////////////////////


    /**
     * Whether the properties of this domain object can be edited, or collections of this object be added to/removed from.
     *
     * <p>
     *     Note that non-editable objects can nevertheless have actions invoked upon them.
     * </p>
     */
    Editing editing() default Editing.AS_CONFIGURED;


    /**
     * If {@link #editing()} is set to {@link Editing#DISABLED},
     * then the reason to provide to the user as to why the object's properties cannot be edited/collections modified.
     */
    String editingDisabledReason() default "Disabled";


    // //////////////////////////////////////


    /**
     * Provides a unique abbreviation for the object type, eg &quot;CUS&quot; for Customer.
     *
     * <p>
     * This value, if specified, is used in the serialized form of the object's OID.  An OID is
     * used by the framework to unique identify an object over time (same concept as a URN).
     * </p>
     */
    String objectType() default "";


    // //////////////////////////////////////


    /**
     * The nature of this domain object.
     */
    Nature nature() default Nature.NOT_SPECIFIED;

}