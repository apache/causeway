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

package org.apache.isis.core.metamodel.services.title;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ManagedObjects.EntityUtil;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@Named("isis.metamodel.TitleServiceDefault")
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

        if(EntityUtil.isDetachedOrRemoved(objectAdapter)) {
            return "[DETACHED]";
        } else {
            return objectAdapter.getSpecification().getTitle(null, objectAdapter);
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

    private Object unwrapped(Object domainObject) {
        return wrapperFactory != null ? wrapperFactory.unwrap(domainObject) : domainObject;
    }

}
