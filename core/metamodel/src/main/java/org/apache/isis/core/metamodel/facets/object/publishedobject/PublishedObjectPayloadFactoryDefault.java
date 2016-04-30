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
package org.apache.isis.core.metamodel.facets.object.publishedobject;

import org.apache.isis.applib.annotation.PublishedObject;
import org.apache.isis.applib.annotation.PublishingChangeKind;
import org.apache.isis.applib.annotation.PublishingPayloadFactoryForObject;
import org.apache.isis.applib.services.publish.EventPayload;
import org.apache.isis.applib.services.publish.EventPayloadForObjectChanged;

public class PublishedObjectPayloadFactoryDefault implements PublishedObject.PayloadFactory {

    private final PublishingPayloadFactoryForObject payloadFactory;

    public PublishedObjectPayloadFactoryDefault() {
        this(null);
    }

    PublishedObjectPayloadFactoryDefault(final PublishingPayloadFactoryForObject payloadFactory) {
        this.payloadFactory = payloadFactory;
    }

    @Override
    public EventPayload payloadFor(
            final Object changedObject,
            final PublishedObject.ChangeKind changeKind) {
        return payloadFactory != null
                ? payloadFactory.payloadFor(changedObject, PublishingChangeKind.from(changeKind))
                : new EventPayloadForObjectChanged<Object>(changedObject);
    }

    /**
     * For testing only.
     */
    public PublishingPayloadFactoryForObject getPayloadFactory() {
        return payloadFactory;
    }
}
