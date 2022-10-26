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
package org.apache.causeway.core.metamodel.objectmanager;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;

import org.apache.causeway.commons.internal._Constants;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.factory._InstanceUtil;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.services.objectlifecycle.ObjectLifecyclePublisher;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Handles injection and lifecycle callbacks.
 *
 * @since 2.0
 */
interface ObjectCreator {

    ManagedObject createObject(ObjectSpecification objectSpecification);

    // -- FACTORY

    public static ObjectCreator createDefault(final MetaModelContext metaModelContext) {
        return new DefaultCreationHandler(metaModelContext);
    }

    @Value @Log4j2
    public static class DefaultCreationHandler implements ObjectCreator {

        @Getter
        private final @NonNull MetaModelContext metaModelContext;

        @Getter(lazy = true, value = AccessLevel.PRIVATE)
        private final ObjectLifecyclePublisher persistenceLifecyclePublisher =
            getMetaModelContext().getServiceRegistry()
                    .lookupServiceElseFail(ObjectLifecyclePublisher.class);


        @Override
        public ManagedObject createObject(final ObjectSpecification spec) {

            if (log.isDebugEnabled()) {
                log.debug("creating instance of {}", spec);
            }

            val pojo = instantiate(spec); // can only be a scalar
            val domainObject = ManagedObject.adaptSingular(spec, pojo);

            // initialize new object
            domainObject.getSpecification().streamAssociations(MixedIn.EXCLUDED)
            .forEach(field->field.toDefault(domainObject));

            getPersistenceLifecyclePublisher().onPostCreate(domainObject);

            return domainObject;

        }

        //  -- HELPER

        private Object instantiate(final ObjectSpecification spec) {

            val type = spec.getCorrespondingClass();
            if (type.isArray()) {
                return Array.newInstance(type.getComponentType(), 0);
            }

            if (Modifier.isAbstract(type.getModifiers())) {
                throw _Exceptions.unrecoverable("Cannot create an instance of an abstract class: " + type);
            }

            try {

                val newInstance = _InstanceUtil.createInstance(type, _Constants.emptyObjects);
                return newInstance;

            } catch (Exception  e) {
                throw _Exceptions.unrecoverable(e,
                        "Failed to create instance of type %s", spec.getFullIdentifier());
            }

        }

    }

}
