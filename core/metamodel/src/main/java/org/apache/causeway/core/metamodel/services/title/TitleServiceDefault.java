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

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.title.TitleService;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;
import org.apache.causeway.core.metamodel.facets.object.title.TitleRenderRequest;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmEntityUtil;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;

import lombok.RequiredArgsConstructor;
import lombok.val;

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

        if(objectManager == null) { // simplified JUnit test support
            return "" + domainObject;
        }

        val pojo = unwrapped(domainObject);
        val objectAdapter = objectManager.adapt(pojo);

        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(objectAdapter)) {
            return "[UNSPECIFIED]";
        }

        if(MmEntityUtil.isDetachedCannotReattach(objectAdapter)) {
            return "[DETACHED]";
        } else {
            return objectAdapter.getSpecification().getTitle(
                    TitleRenderRequest.builder()
                    .object(objectAdapter)
                    .build());
        }
    }

    @Override
    public String iconNameOf(final Object domainObject) {

        if(objectManager == null) { // simplified JUnit test support
            return domainObject!=null ? domainObject.getClass().getSimpleName() : "null";
        }

        val pojo = unwrapped(domainObject);
        val objectAdapter = objectManager.adapt(pojo);

        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(objectAdapter)) {
            return "unspecified";
        }

        return objectAdapter.getSpecification().getIconName(objectAdapter);
    }

    //-- HELPER

    private Object unwrapped(final Object domainObject) {
        return wrapperFactory != null ? wrapperFactory.unwrap(domainObject) : domainObject;
    }

}
