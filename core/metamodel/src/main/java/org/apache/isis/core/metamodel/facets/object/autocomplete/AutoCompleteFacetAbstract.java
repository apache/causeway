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

package org.apache.isis.core.metamodel.facets.object.autocomplete;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.param.autocomplete.MinLengthUtil;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.publishing.PublishingServiceInternal;

public abstract class AutoCompleteFacetAbstract extends FacetAbstract implements AutoCompleteFacet {

    public static Class<? extends Facet> type() {
        return AutoCompleteFacet.class;
    }

    private final AuthenticationSessionProvider authenticationSessionProvider;
    private final ObjectAdapterProvider adapterProvider;
    private final ServicesInjector servicesInjector;
    private final Class<?> repositoryClass;
    private final Method repositoryMethod;

    /**
     * lazily populated
     */
    private Integer minLength;

    public AutoCompleteFacetAbstract(
            final FacetHolder holder,
            final Class<?> repositoryClass,
            final Method repositoryMethod,
            final ServicesInjector servicesInjector) {
        super(type(), holder, Derivation.NOT_DERIVED);

        this.repositoryClass = repositoryClass;
        this.repositoryMethod = repositoryMethod;

        this.adapterProvider = servicesInjector.getPersistenceSessionServiceInternal();
        this.servicesInjector = servicesInjector;
        this.authenticationSessionProvider = servicesInjector.getAuthenticationSessionProvider();
    }

    public Class<?> getRepositoryClass() {
        return repositoryClass;
    }

    @Override
    public List<ObjectAdapter> execute(
            final String search,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final ObjectAdapter resultAdapter =
                getPublishingServiceInternal().withPublishingSuppressed(new PublishingServiceInternal.Block<ObjectAdapter>() {
                    @Override
                    public ObjectAdapter exec() {
                        final Object list = invoke();
                        return adapterProvider.adapterFor(list);
                    }

                    private Object invoke()  {
                        try {
                            return repositoryMethod.invoke(getRepository(), search);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            return Collections.emptyList();
                        }
                    }
                });

        // check a collection was returned
        if(CollectionFacet.Utils.getCollectionFacetFromSpec(resultAdapter) == null) {
            return Collections.emptyList();
        }

        final Stream<ObjectAdapter> adapterList = CollectionFacet.Utils.streamAdapters(resultAdapter) ;

        return ObjectAdapter.Util.visibleAdapters(adapterList, interactionInitiatedBy);
    }

    private Object getRepository() {
        return servicesInjector.lookupService(repositoryClass).orElse(null);
    }

    private PublishingServiceInternal getPublishingServiceInternal() {
        return servicesInjector.lookupServiceElseFail(PublishingServiceInternal.class);
    }


    @Override
    public int getMinLength() {
        if(minLength == null) {
            minLength = MinLengthUtil.determineMinLength(repositoryMethod);
        }
        return minLength;
    }

    protected AuthenticationSession getAuthenticationSession() {
        return authenticationSessionProvider.getAuthenticationSession();
    }

    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("repositoryClass", repositoryClass);
        attributeMap.put("repositoryMethod", repositoryMethod);
        attributeMap.put("minLength", getMinLength());
    }
}
