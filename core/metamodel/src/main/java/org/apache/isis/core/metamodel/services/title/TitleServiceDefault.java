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

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternal;

@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
        )
public class TitleServiceDefault implements TitleService {

    @Programmatic
    @Override
    public String titleOf(final Object domainObject) {
        final ObjectAdapter objectAdapter = adapterManager.adapterFor(unwrapped(domainObject));
        final boolean destroyed = objectAdapter.isDestroyed();
        if(!destroyed) {
            return objectAdapter.getSpecification().getTitle(null, objectAdapter);
        } else {
            return "[DELETED]";
        }
    }

    @Programmatic
    @Override
    public String iconNameOf(final Object domainObject) {
        final ObjectAdapter objectAdapter = adapterManager.adapterFor(unwrapped(domainObject));
        return objectAdapter.getSpecification().getIconName(objectAdapter);
    }




    // //////////////////////////////////////

    private Object unwrapped(Object domainObject) {
        return wrapperFactory != null ? wrapperFactory.unwrap(domainObject) : domainObject;
    }

    // //////////////////////////////////////

    @javax.inject.Inject
    PersistenceSessionServiceInternal adapterManager;

    @javax.inject.Inject
    WrapperFactory wrapperFactory;

}
