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
package org.apache.causeway.persistence.jdo.datanucleus.changetracking;

import javax.jdo.JDOHelper;
import javax.jdo.ObjectState;
import javax.jdo.listener.InstanceLifecycleEvent;

import org.datanucleus.enhancement.Persistable;

import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.persistence.jdo.datanucleus.entities.DnObjectProviderForCauseway;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
final class _Utils {

    Persistable persistableFor(final InstanceLifecycleEvent event) {
        return (Persistable)event.getPersistentInstance();
    }

    void resolveInjectionPoints(
            final @NonNull MetaModelContext mmc,
            final @NonNull InstanceLifecycleEvent event) {
        final Persistable pojo = _Utils.persistableFor(event);
        if(pojo!=null) {
            DnObjectProviderForCauseway.extractFrom(pojo)
            .ifPresentOrElse(
                    DnObjectProviderForCauseway::injectServicesIfNotAlready,
                    ()->mmc.getServiceInjector().injectServicesInto(pojo));
            }
    }

    ManagedObject adaptEntity(
            final @NonNull MetaModelContext mmc,
            final @NonNull Object entityPojo) {

        val objectManager = mmc.getObjectManager();
        val entity = objectManager.adapt(entityPojo);
        _Assert.assertTrue(entity.getSpecification().isEntity());
        return entity;
    }

    String debug(final InstanceLifecycleEvent event) {
        // try to be side-effect free here ...
        final Persistable pojo = _Utils.persistableFor(event);
        ObjectState state = JDOHelper.getObjectState(pojo);
        //if(state == ObjectState.PERSISTENT_CLEAN) {
            //return String.format("entity: %s", pojo);
        //} else {
            return String.format("entity: %s (%s)", pojo.getClass().getSimpleName(), state);
        //}
    }

}
