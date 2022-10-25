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

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMementoCollection;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMementoForEmpty;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMementoForScalar;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * @since 2.0
 */
@Service
@Named(CausewayModuleCoreMetamodel.NAMESPACE + ".ObjectManagerDefault")
@Priority(PriorityPrecedence.EARLY)
@Qualifier("DEFAULT")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ObjectManagerDefault implements ObjectManager {

    @Getter(onMethod_ = {@Override})
    private final MetaModelContext metaModelContext;

    @Getter(onMethod_ = {@Override}) private ObjectLoader objectLoader;
    @Getter(onMethod_ = {@Override}) private ObjectBulkLoader objectBulkLoader;
    @Getter(onMethod_ = {@Override}) private ObjectCreator objectCreator;
    @Getter(onMethod_ = {@Override}) private ObjectBookmarker objectBookmarker;

    @PostConstruct
    public void init() {
        objectCreator = ObjectCreator.createDefault(metaModelContext);
        objectLoader = ObjectLoader.createDefault(metaModelContext);
        objectBulkLoader = ObjectBulkLoader.createDefault(metaModelContext);
        objectBookmarker = ObjectBookmarker.createDefault();
    }

    @Override
    public ManagedObject demementify(final @Nullable ObjectMemento memento) {

        if(memento==null) {
            return null;
        }

        if(memento instanceof ObjectMementoForEmpty) {
            val objectMementoForEmpty = (ObjectMementoForEmpty) memento;
            val logicalType = objectMementoForEmpty.getLogicalType();
            val spec = getSpecificationLoader().specForLogicalType(logicalType);
            return spec.isPresent()
                    ? ManagedObject.empty(spec.get())
                    : ManagedObject.unspecified();
        }

        if(memento instanceof ObjectMementoCollection) {
            val objectMementoCollection = (ObjectMementoCollection) memento;

            val elementSpec = getSpecificationLoader().specForLogicalTypeNameElseFail(memento.getLogicalTypeName());

            val objects = objectMementoCollection.unwrapList().stream()
                    .map(this::demementify)
                    .collect(Can.toCan());

            return ManagedObject.packed(elementSpec, objects);
        }

        if(memento instanceof ObjectMementoForScalar) {
            val objectMementoAdapter = (ObjectMementoForScalar) memento;
            return objectMementoAdapter.reconstructObject(getMetaModelContext());
        }

        throw _Exceptions.unrecoverable("unsupported ObjectMemento type %s", memento.getClass());
    }


    // JUnit support
    public static ObjectManager forTesting(final MetaModelContext metaModelContext) {
        val objectManager = new ObjectManagerDefault(metaModelContext);
        objectManager.init();
        return objectManager;
    }

}
