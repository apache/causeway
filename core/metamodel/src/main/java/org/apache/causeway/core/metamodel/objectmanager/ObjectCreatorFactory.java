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

import org.apache.causeway.applib.spec.Specification;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.handler.ChainOfResponsibility;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.factory._InstanceUtil;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.services.objectlifecycle.ObjectLifecyclePublisher;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;

import lombok.extern.slf4j.Slf4j;

/**
 * Handles injection and lifecycle callbacks.
 */
@Slf4j
record ObjectCreatorFactory() {

    static ChainOfResponsibility<ObjectSpecification, ManagedObject> createChain(final MetaModelContext mmc) {
        return new ChainOfResponsibility<>("ObjectCreator", Can.of(new BuiltinHandler(mmc)));
    }

    // -- HANDLERS

    record BuiltinHandler(
            _Lazy<ObjectLifecyclePublisher> persistenceLifecyclePublisher)
    implements ChainOfResponsibility.Handler<ObjectSpecification, ManagedObject> {

        BuiltinHandler(final MetaModelContext mmc) {
            this(_Lazy.threadSafe(()->mmc.getServiceRegistry()
                    .lookupServiceElseFail(ObjectLifecyclePublisher.class)));
        }

        @Override
        public boolean isHandling(final ObjectSpecification spec) {
            return true;
        }

        @Override
        public ManagedObject handle(final ObjectSpecification spec) {
            log.debug("creating instance of {}", spec);

            var pojo = instantiate(spec); // can only be a scalar
            if(Specification.class.isAssignableFrom(spec.getCorrespondingClass())
                    || !spec.isValue()) {
                spec.getServiceInjector().injectServicesInto(pojo);
            }
            var domainObject = ManagedObject.adaptSingular(spec, pojo);

            // initialize new object
            domainObject.objSpec().streamAssociations(MixedIn.EXCLUDED)
                .forEach(field->field.toDefault(domainObject));

            if (domainObject.objSpec().isEntity()) {
                persistenceLifecyclePublisher().get().onPostCreate(domainObject);
            }

            return domainObject;
        }

        //  -- HELPER

        private Object instantiate(final ObjectSpecification spec) {
            var type = spec.getCorrespondingClass();
            if (type.isArray()) {
                return Array.newInstance(type.getComponentType(), 0);
            }

            if (Modifier.isAbstract(type.getModifiers())) {
                throw _Exceptions.unrecoverable("Cannot create an instance of an abstract class: " + type);
            }

            try {
                var newInstance = _InstanceUtil.createInstance(type);
                return newInstance;
            } catch (Exception  e) {
                throw _Exceptions.unrecoverable(e,
                        "Failed to create instance of type %s", spec.getFullIdentifier());
            }
        }

    }
}
