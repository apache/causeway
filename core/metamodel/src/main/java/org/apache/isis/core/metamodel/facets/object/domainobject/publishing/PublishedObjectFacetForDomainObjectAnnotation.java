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

package org.apache.isis.core.metamodel.facets.object.domainobject.publishing;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.annotation.PublishingPayloadFactoryForObject;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.publishedobject.PublishedObjectFacet;
import org.apache.isis.core.metamodel.facets.object.publishedobject.PublishedObjectFacetAbstract;

public class PublishedObjectFacetForDomainObjectAnnotation extends PublishedObjectFacetAbstract {

    public static PublishedObjectFacet create(
            final DomainObject domainObject,
            final IsisConfiguration configuration,
            final FacetHolder holder) {

        Publishing publishing = Publishing.AS_CONFIGURED;
        if(domainObject!=null){
            switch (domainObject.entityChangePublishing()){
                case DISABLED:
                    publishing=Publishing.DISABLED;
                    break;
                case ENABLED:
                    publishing=Publishing.ENABLED;
                    break;
                default:
                    publishing=domainObject.publishing();
            }
        }
        switch (publishing) {
            case AS_CONFIGURED:

                final PublishObjectsConfiguration setting = PublishObjectsConfiguration.parse(configuration);
                switch (setting) {
                    case NONE:
                        return null;
                    default:
                        final PublishingPayloadFactoryForObject publishingPayloadFactory = newPayloadFactory(domainObject);
                        return domainObject != null
                            ? new PublishedObjectFacetForDomainObjectAnnotationAsConfigured(publishingPayloadFactory, holder)
                            : new PublishedObjectFacetFromConfiguration(publishingPayloadFactory, holder);
                }
            case DISABLED:
                return null;
            case ENABLED:
                return new PublishedObjectFacetForDomainObjectAnnotation(
                        newPayloadFactory(domainObject), holder);
        }
        return null;
    }

    /**
     * @return null means that the default payload factories will be used; this is handled within IsisTransaction.
     */
    protected static PublishingPayloadFactoryForObject newPayloadFactory(final DomainObject domainObject) {
        if(domainObject == null) {
            return null;
        }
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

    PublishedObjectFacetForDomainObjectAnnotation(final PublishingPayloadFactoryForObject publishingPayloadFactory, final FacetHolder holder) {
        super(publishingPayloadFactory, holder);
    }

}
