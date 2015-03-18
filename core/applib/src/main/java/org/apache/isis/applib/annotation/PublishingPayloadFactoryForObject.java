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

import org.apache.isis.applib.services.publish.EventPayload;

public interface PublishingPayloadFactoryForObject {

    @Programmatic
    public EventPayload payloadFor(Object changedObject, PublishingChangeKind publishingChangeKind);

    /**
     * Adapter to subclass if have an existing {@link org.apache.isis.applib.annotation.PublishedObject.PayloadFactory}.
     */
    @Deprecated
    public static class Adapter implements PublishingPayloadFactoryForObject {

        private final PublishedObject.PayloadFactory payloadFactory;

        public Adapter(final PublishedObject.PayloadFactory payloadFactory) {
            this.payloadFactory = payloadFactory;
        }

        @Override
        public EventPayload payloadFor(final Object changedObject, final PublishingChangeKind publishingChangeKind) {
            return payloadFactory.payloadFor(changedObject, PublishingChangeKind.from(publishingChangeKind));
        }

        public PublishedObject.PayloadFactory getPayloadFactory() {
            return payloadFactory;
        }
    }
}
