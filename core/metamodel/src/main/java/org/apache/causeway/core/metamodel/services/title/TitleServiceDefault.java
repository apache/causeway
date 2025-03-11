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
package org.apache.causeway.core.metamodel.services.title;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.title.TitleService;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;
import org.apache.causeway.core.metamodel.facets.object.title.TitleRenderRequest;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmEntityUtils;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;

import lombok.RequiredArgsConstructor;

/**
 * Default implementation of {@link TitleService}.
 *
 * @since 1.x revised for 2.0 {@index}
 */
@Service
@Named(CausewayModuleCoreMetamodel.NAMESPACE + ".TitleServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TitleServiceDefault implements TitleService {

    private final WrapperFactory wrapperFactory;
    private final ObjectManager objectManager;

    @Override
    public String titleOf(final Object domainObject) {

        if(objectManager == null) return "" + domainObject; // simplified JUnit test support

        var pojo = unwrapped(domainObject);
        var objectAdapter = objectManager.adapt(pojo);

        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(objectAdapter)) return "[UNSPECIFIED]";

        return MmEntityUtils.getEntityState(objectAdapter).isTransientOrRemoved()
            // here we just mean NOT-ATTACHED (and not the concrete DETACHED entity state)
            ? "[DETACHED]"
            : objectAdapter.objSpec().getTitle(TitleRenderRequest.forObject(objectAdapter));
    }

    @Override
    public String iconNameOf(final Object domainObject) {

        if(objectManager == null)  // simplified JUnit test support
            return domainObject!=null ? domainObject.getClass().getSimpleName() : "null";

        var pojo = unwrapped(domainObject);
        var objectAdapter = objectManager.adapt(pojo);

        return ManagedObjects.isNullOrUnspecifiedOrEmpty(objectAdapter)
            ? "unspecified"
            : objectAdapter.objSpec().getIconName(objectAdapter);
    }

    //-- HELPER

    private Object unwrapped(final Object domainObject) {
        return wrapperFactory != null ? wrapperFactory.unwrap(domainObject) : domainObject;
    }

}
