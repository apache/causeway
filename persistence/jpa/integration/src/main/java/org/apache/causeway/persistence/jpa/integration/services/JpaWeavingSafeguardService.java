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
package org.apache.causeway.persistence.jpa.integration.services;

import java.util.HashSet;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.causeway.applib.events.metamodel.MetamodelListener;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.beans.CausewayBeanMetaData.PersistenceStack;
import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistry;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JpaWeavingSafeguardService implements MetamodelListener {

    @Inject CausewayConfiguration config;
    @Inject CausewayBeanTypeRegistry causewayBeanTypeRegistry;

    @Override public void onMetamodelLoaded() { }
    @Override public void onMetamodelAboutToBeLoaded() {
        log.info("running JPA Weaving Safeguard ...");
        var jpaWeavingSafeguard = new JpaWeavingSafeguard(config.persistence().weaving().safeguardMode());
        jpaWeavingSafeguard.checkAll(causewayBeanTypeRegistry
                .streamEntityTypes(PersistenceStack.JPA)
                .collect(Collectors.toCollection(HashSet::new)));
    }

}

