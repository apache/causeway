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

package org.apache.isis.core.metamodel.facets.actions.action.publishing;

import org.apache.isis.applib.annotation.PublishedAction;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.actions.publish.PublishedActionFacet;
import org.apache.isis.core.metamodel.facets.actions.publish.PublishedActionFacetAbstract;

/**
 * @deprecated
 */
@Deprecated
public class PublishedActionFacetForPublishedActionAnnotation extends PublishedActionFacetAbstract {

    public static PublishedActionFacet create(
            final PublishedAction publishedAction,
            final FacetHolder holder) {

        if (publishedAction == null) {
            return null;
        }

        return new PublishedActionFacetForPublishedActionAnnotation(newPayloadFactory(publishedAction), holder);
    }


    public PublishedActionFacetForPublishedActionAnnotation(
            final PublishedAction.PayloadFactory payloadFactory,
            final FacetHolder holder) {
        super(payloadFactory, holder);
    }

    private static PublishedAction.PayloadFactory newPayloadFactory(final PublishedAction publishedAction) {
        final Class<? extends PublishedAction.PayloadFactory> payloadFactoryClass = publishedAction.value();
        if(payloadFactoryClass == null) {
            return null;
        }

        try {
            return payloadFactoryClass.newInstance();
        } catch (final InstantiationException e) {
            return null;
        } catch (final IllegalAccessException e) {
            return null;
        }
    }


}
