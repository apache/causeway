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

package org.apache.isis.core.metamodel.facets.object.domainobject;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.PublishedObject;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.annotation.PublishingChangeKind;
import org.apache.isis.applib.annotation.PublishingPayloadFactoryForObject;
import org.apache.isis.applib.services.publish.EventPayload;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.publishedobject.PublishedObjectFacet;
import org.apache.isis.core.metamodel.facets.object.publishedobject.PublishedObjectFacetAbstract;

public class PublishedObjectFacetForDomainObjectAnnotation extends PublishedObjectFacetAbstract {

    public static PublishedObjectFacet create(
            final DomainObject domainObject,
            final IsisConfiguration configuration,
            final FacetHolder holder) {

        final Publishing publishing = domainObject != null ? domainObject.publishing() : Publishing.AS_CONFIGURED;

        switch (publishing) {
            case AS_CONFIGURED:

                final PublishObjectsConfiguration setting = PublishObjectsConfiguration.parse(configuration);
                switch (setting) {
                    case NONE:
                        return null;
                    default:
                    return new PublishedObjectFacetForDomainObjectAnnotationAsConfigured(
                            newPayloadFactory(domainObject), holder);
                }
            case DISABLED:
                return null;
            case ENABLED:
                return new PublishedObjectFacetForDomainObjectAnnotation(
                        newPayloadFactory(domainObject), holder);
        }
        return null;
    }

    protected static PublishingPayloadFactoryForObject newPayloadFactory(
            final DomainObject domainObject) {
        final Class<? extends PublishingPayloadFactoryForObject> value = domainObject.publishingPayloadFactory();
        if(value == null) {
            return null;
        }
        try {
            return value.newInstance();
        } catch (final InstantiationException e) {
            return null;
        } catch (final IllegalAccessException e) {
            return null;
        }
    }


    PublishedObjectFacetForDomainObjectAnnotation(PublishingPayloadFactoryForObject publishingPayloadFactory, final FacetHolder holder) {
        super(legacyPayloadFactoryFor(publishingPayloadFactory), holder);
    }

    private static PublishedObject.PayloadFactory legacyPayloadFactoryFor(PublishingPayloadFactoryForObject publishingPayloadFactory) {
        if(publishingPayloadFactory instanceof PublishingPayloadFactoryForObject.Adapter) {
            final PublishingPayloadFactoryForObject.Adapter adapter = (PublishingPayloadFactoryForObject.Adapter) publishingPayloadFactory;
            return adapter.getPayloadFactory();
        }
        return new LegacyAdapter(publishingPayloadFactory);
    }

    private static class LegacyAdapter implements PublishedObject.PayloadFactory {

        private final PublishingPayloadFactoryForObject payloadFactory;

        LegacyAdapter(final PublishingPayloadFactoryForObject payloadFactory) {
            this.payloadFactory = payloadFactory;
        }

        @Override
        public EventPayload payloadFor(Object changedObject, PublishedObject.ChangeKind changeKind) {
            return payloadFactory.payloadFor(changedObject, PublishingChangeKind.from(changeKind));
        }
    }

}
