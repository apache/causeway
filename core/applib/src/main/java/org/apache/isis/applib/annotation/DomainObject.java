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
import org.apache.isis.applib.services.publish.EventPayload;

/**
 * Domain semantics for domain objects (entities and view models; for services see {@link org.apache.isis.applib.annotation.DomainService}).
 */
@Inherited
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface DomainObject {

    public enum AuditingPolicy {
        /**
         * The auditing of the object should be as per the default auditing policy configured in <tt>isis.properties</tt>.
         *
         * <p>
         *     If no auditing policy is configured, then the auditing is disabled.
         * </p>
         */
        AS_CONFIGURED,
        /**
         * Audit changes to this object.
         */
        ENABLED,
        /**
         * Do not audit changes to this object (even if otherwise configured to enable auditing).
         */
        DISABLED
    }

    /**
     * Whether the entity should be audited (note: does not apply to {@link #viewModel() view model}s.
     *
     * <p>
     * Requires that an implementation of the {@link org.apache.isis.applib.services.audit.AuditingService3} is
     * registered with the framework.
     * </p>
     */
    AuditingPolicy auditing() default AuditingPolicy.AS_CONFIGURED;


    // //////////////////////////////////////


    public enum PublishingChangeKind {
        CREATE,
        UPDATE,
        DELETE;

        @Deprecated
        public static PublishedObject.ChangeKind from(final PublishingChangeKind publishingChangeKind) {
            if(publishingChangeKind == CREATE) return PublishedObject.ChangeKind.CREATE;
            if(publishingChangeKind == UPDATE) return PublishedObject.ChangeKind.UPDATE;
            if(publishingChangeKind == DELETE) return PublishedObject.ChangeKind.DELETE;
            // shouldn't happen
            throw new IllegalArgumentException("Unrecognized changeKind: " + publishingChangeKind);
        }
        @Deprecated
        public static PublishingChangeKind from(final PublishedObject.ChangeKind  publishingChangeKind) {
            if(publishingChangeKind == PublishedObject.ChangeKind.CREATE) return CREATE;
            if(publishingChangeKind == PublishedObject.ChangeKind.UPDATE) return UPDATE;
            if(publishingChangeKind == PublishedObject.ChangeKind.DELETE) return DELETE;
            // shouldn't happen
            throw new IllegalArgumentException("Unrecognized changeKind: " + publishingChangeKind);
        }
    }

    public interface PublishingPayloadFactory {

        @Programmatic
        public EventPayload payloadFor(Object changedObject, PublishingChangeKind publishingChangeKind);

        /**
         * Adapter to subclass if have an existing {@link org.apache.isis.applib.annotation.PublishedObject.PayloadFactory}.
         */
        @Deprecated
        public abstract class Adapter implements PublishingPayloadFactory {

            private final PublishedObject.PayloadFactory payloadFactory;

            public Adapter(final PublishedObject.PayloadFactory payloadFactory) {
                this.payloadFactory = payloadFactory;
            }

            @Override
            public EventPayload payloadFor(Object changedObject, PublishingChangeKind publishingChangeKind) {
                return payloadFactory.payloadFor(changedObject, PublishingChangeKind.from(publishingChangeKind));
            }
        }
    }

    // TODO: factor out PayloadFactory.Default so similar to interaction
    Class<? extends PublishingPayloadFactory> publishingPayloadFactory() default PublishingPayloadFactory.class;


    // //////////////////////////////////////


    /**
     * The class of the domain service that provides an <code>autoComplete(String)</code> method.
     *
     * <p>
     * It is sufficient to specify an interface rather than a concrete type.
     */
    Class<?> autoCompleteRepository();


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
     *
     * <p>
     *     Corresponds to the {@link org.apache.isis.applib.annotation.Immutable} annotation).
     * </p>
     */
    boolean notEditable() default false;


    /**
     * If {@link #notEditable()}, then the reason to provide to the user as to why the object's properties cannot be
     * edited.
     */
    String notEditableReason();


    // //////////////////////////////////////


    /**
     * Provides a unique abbreviation for the object type, eg &quot;CUS&quot; for Customer.
     *
     * <p>
     * This value, if specified, is used in the serialized form of the object's OID.  An OID is
     * used by the framework to unique identify an object over time (same concept as a URN).
     * </p>
     */
    String objectType();


    // //////////////////////////////////////

    /**
     * Indicates that instances of this class should be handled as a view model.
     *
     * <p>
     *     All of the view model object's read/write and non-{@link Programmatic} properties (not collections) will
     *     be included in the memento; in other words as returned and marshalled by
     *     {@link org.apache.isis.applib.ViewModel#viewModelMemento()}.
     * </p>
     */
    boolean viewModel() default false;

}