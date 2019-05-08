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
package org.apache.isis.core.runtime.services.homepage;

import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Singleton;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.homepage.HomePageProviderService;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.metamodel.MetaModelContext;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.actions.homepage.HomePageFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

import lombok.val;

//FIXME [2033] looks like a duplicate of ManagedObjectContextBase_findHomepage
@Singleton
public class HomePageProviderServiceDefault implements HomePageProviderService {
    
    @Programmatic
    @Override
    public Object homePage() {
        return homePage.get();
    }
    
    private final _Lazy<Object> homePage = _Lazy.of(this::lookupHomePage);

    private Object lookupHomePage() {
        
        val metaModelContext = MetaModelContext.current();
        
        final Stream<ObjectAdapter> serviceAdapters = metaModelContext.streamServiceAdapters(); 
                
        return serviceAdapters.map(serviceAdapter->{
            final ObjectSpecification serviceSpec = serviceAdapter.getSpecification();
            final Stream<ObjectAction> objectActions = serviceSpec.streamObjectActions(Contributed.EXCLUDED);
            
            final Optional<Object> homePage = objectActions
            .map(objectAction->homePageIfUsable(serviceAdapter, objectAction))
            .filter(_NullSafe::isPresent)
            .findAny();
            
            return homePage;
        })
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findAny()
        .orElse(null);
    }

    protected Object homePageIfUsable(ObjectAdapter serviceAdapter, ObjectAction objectAction) {
        if (!objectAction.containsDoOpFacet(HomePageFacet.class)) {
            return null;
        }

        final Consent visibility =
                objectAction.isVisible(
                        serviceAdapter,
                        InteractionInitiatedBy.USER,
                        Where.ANYWHERE);
        if (visibility.isVetoed()) {
            return null;
        }

        final Consent usability =
                objectAction.isUsable(
                        serviceAdapter,
                        InteractionInitiatedBy.USER,
                        Where.ANYWHERE
                        );
        if (usability.isVetoed()) {
            return  null;
        }

        final ObjectAdapter mixedInAdapter = null;
        final ObjectAdapter[] parameters = {};

        final ObjectAdapter objectAdapter = objectAction.executeWithRuleChecking(
                serviceAdapter, mixedInAdapter, parameters,
                InteractionInitiatedBy.USER,
                WHERE_FOR_ACTION_INVOCATION);

        return objectAdapter != null ? objectAdapter.getPojo(): null;
    }

    // REVIEW: should provide this rendering context, rather than hardcoding.
    // the net effect currently is that class members annotated with
    // @Hidden(where=Where.ANYWHERE) or @Disabled(where=Where.ANYWHERE) will indeed
    // be hidden/disabled, but will be visible/enabled (perhaps incorrectly)
    // for any other value for Where
    protected static final Where WHERE_FOR_ACTION_INVOCATION = Where.ANYWHERE;

}
