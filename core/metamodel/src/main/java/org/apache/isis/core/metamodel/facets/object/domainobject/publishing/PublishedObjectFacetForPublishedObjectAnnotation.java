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

import org.apache.isis.applib.annotation.PublishedObject;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.publishedobject.PublishedObjectFacet;
import org.apache.isis.core.metamodel.facets.object.publishedobject.PublishedObjectFacetAbstract;

@Deprecated
public class PublishedObjectFacetForPublishedObjectAnnotation extends PublishedObjectFacetAbstract {

    public static PublishedObjectFacet create(
            final PublishedObject publishedObject,
            final FacetHolder holder) {

        if (publishedObject == null) {
            return null;
        }

        else return new PublishedObjectFacetForPublishedObjectAnnotation(newPayloadFactory(publishedObject.value()), holder);
    }

    private PublishedObjectFacetForPublishedObjectAnnotation(final PublishedObject.PayloadFactory payloadFactory, final FacetHolder holder) {
        super(payloadFactory, holder);
    }

    private static PublishedObject.PayloadFactory newPayloadFactory(final Class<? extends PublishedObject.PayloadFactory> value) {
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

}
