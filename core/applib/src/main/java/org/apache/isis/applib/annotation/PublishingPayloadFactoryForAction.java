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

import java.util.List;
import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.publish.EventPayload;
import org.apache.isis.applib.services.publish.PublisherService;
import org.apache.isis.applib.services.publish.PublishingService;

/**
 * @deprecated - supports the deprecated {@link PublishingService}; use instead {@link PublisherService} (which ignores {@link Action#publishingPayloadFactory()} but is otherwise more flexible.
 */
@Deprecated
public interface PublishingPayloadFactoryForAction {

    /**
     * @deprecated - because {@link PublishingService} is deprecated.
     */
    @Deprecated
    @Programmatic
    public EventPayload payloadFor(Identifier actionIdentifier, Object target, List<Object> arguments, Object result);

    /**
     * Adapter to subclass if have an existing {@link org.apache.isis.applib.annotation.PublishedObject.PayloadFactory}.
     *
     * @deprecated
     */
    @Deprecated
    public abstract class Adapter implements PublishingPayloadFactoryForAction {

        private final PublishedAction.PayloadFactory payloadFactory;

        /**
         * @deprecated
         */
        @Deprecated
        public Adapter(final PublishedAction.PayloadFactory payloadFactory) {
            this.payloadFactory = payloadFactory;
        }

        /**
         * @deprecated
         */
        @Deprecated
        @Override
        public EventPayload payloadFor(Identifier actionIdentifier, Object target, List<Object> arguments, Object result) {
            return payloadFactory.payloadFor(actionIdentifier, target, arguments, result);
        }

        /**
         * @deprecated
         */
        @Deprecated
        public PublishedAction.PayloadFactory getPayloadFactory() {
            return payloadFactory;
        }
    }
}
