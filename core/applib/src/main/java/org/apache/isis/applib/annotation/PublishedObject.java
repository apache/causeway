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
 * @deprecated - use {@link org.apache.isis.applib.annotation.DomainObject#publishingPayloadFactory()} instead.
 */
@Deprecated
@Inherited
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface PublishedObject {

    /**
     * @deprecated - use {@link PublishingChangeKind} instead.
     */
    @Deprecated
    public enum ChangeKind {
        /**
         * @deprecated - use {@link PublishingChangeKind#CREATE} instead.
         */
        @Deprecated
        CREATE,
        /**
         * @deprecated - use {@link PublishingChangeKind#UPDATE} instead.
         */
        @Deprecated
        UPDATE,
        /**
         * @deprecated - use {@link PublishingChangeKind#DELETE} instead.
         */
        @Deprecated
        DELETE
    }

    /**
     * @deprecated - use {@link PublishingPayloadFactoryForObject} instead.
     */
    @Deprecated
    public interface PayloadFactory {
        /**
         * @deprecated - use {@link PublishingPayloadFactoryForObject#payloadFor(Object, PublishingChangeKind)} instead.
         */
        @Deprecated
        @Programmatic
        public EventPayload payloadFor(Object changedObject, ChangeKind changeKind);
    }

    /**
     * @deprecated - use {@link DomainObject#publishingPayloadFactory()} instead.
     */
    @Deprecated
    Class<? extends PayloadFactory> value() default PayloadFactory.class;
}
