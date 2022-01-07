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
package org.apache.isis.core.metamodel.objectmanager;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotations.PriorityPrecedence;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.objectmanager.create.ObjectCreator;
import org.apache.isis.core.metamodel.objectmanager.detach.ObjectDetacher;
import org.apache.isis.core.metamodel.objectmanager.identify.ObjectBookmarker;
import org.apache.isis.core.metamodel.objectmanager.load.ObjectLoader;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMemorizer;
import org.apache.isis.core.metamodel.objectmanager.query.ObjectBulkLoader;
import org.apache.isis.core.metamodel.objectmanager.refresh.ObjectRefresher;
import org.apache.isis.core.metamodel.objectmanager.serialize.ObjectSerializer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 *
 * @since 2.0
 *
 */
@Service
@Named("isis.metamodel.ObjectManagerDefault")
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
    @Getter(onMethod_ = {@Override}) private ObjectRefresher objectRefresher;
    @Getter(onMethod_ = {@Override}) private ObjectDetacher objectDetacher;
    @Getter(onMethod_ = {@Override}) private ObjectSerializer objectSerializer;
    @Getter(onMethod_ = {@Override}) private ObjectMemorizer objectMemorizer;

    @PostConstruct
    public void init() {
        objectCreator = ObjectCreator.createDefault(metaModelContext);
        objectLoader = ObjectLoader.createDefault(metaModelContext);
        objectBulkLoader = ObjectBulkLoader.createDefault(metaModelContext);
        objectBookmarker = ObjectBookmarker.createDefault();
        objectRefresher = ObjectRefresher.createDefault();
        objectDetacher = ObjectDetacher.createDefault(metaModelContext);
        objectSerializer = ObjectSerializer.createDefault(metaModelContext);
        objectMemorizer = ObjectMemorizer.createDefault(metaModelContext);
    }


    // JUnit support
    public static ObjectManager forTesting(final MetaModelContext metaModelContext) {

        val objectManager = new ObjectManagerDefault(metaModelContext);
        objectManager.init();
        return objectManager;
    }

}
