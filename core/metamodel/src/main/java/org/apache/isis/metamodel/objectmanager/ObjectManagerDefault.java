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
package org.apache.isis.metamodel.objectmanager;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Service;

import org.apache.isis.metamodel.context.MetaModelContext;
import org.apache.isis.metamodel.objectmanager.create.ObjectCreator;
import org.apache.isis.metamodel.objectmanager.identify.ObjectIdentifier;
import org.apache.isis.metamodel.objectmanager.load.ObjectLoader;
import org.apache.isis.metamodel.objectmanager.refresh.ObjectRefresher;

import lombok.Getter;
import lombok.val;

/**
 * 
 * @since 2.0
 *
 */
@Service @Named("objectManagerDefault")
public class ObjectManagerDefault implements ObjectManager {
    
    @Inject @Getter(onMethod = @__(@Override)) private MetaModelContext metaModelContext;
    
    @Getter(onMethod = @__(@Override)) private ObjectLoader objectLoader;
    @Getter(onMethod = @__(@Override)) private ObjectCreator objectCreator;
    @Getter(onMethod = @__(@Override)) private ObjectIdentifier objectIdentifier;
    @Getter(onMethod = @__(@Override)) private ObjectRefresher objectRefresher;
    
    @PostConstruct
    public void init() {
        objectCreator = ObjectCreator.createDefault(metaModelContext);
        objectLoader = ObjectLoader.createDefault(metaModelContext);
        objectIdentifier = ObjectIdentifier.createDefault();
        objectRefresher = ObjectRefresher.createDefault();
    }

    
    // JUnit support
    public static ObjectManager forTesting(MetaModelContext metaModelContext) {

        val objectManager = new ObjectManagerDefault();
        objectManager.metaModelContext = metaModelContext;
        objectManager.init();
        return objectManager;
    }

}
