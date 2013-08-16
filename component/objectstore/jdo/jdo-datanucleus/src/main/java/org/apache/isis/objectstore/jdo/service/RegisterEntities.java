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
package org.apache.isis.objectstore.jdo.service;

import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jdo.annotations.PersistenceCapable;

import com.google.common.base.Splitter;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.core.runtime.system.context.IsisContext;

@Hidden
public class RegisterEntities {

    private final static Logger LOG = LoggerFactory.getLogger(RegisterEntities.class);
    
    private final static String PACKAGE_PREFIX_KEY = "isis.persistor.datanucleus.RegisterEntities.packagePrefix";

    private String packagePrefixes;

    @PostConstruct
    public void init(Map<String,String> props) {
        packagePrefixes = props.get(PACKAGE_PREFIX_KEY);
    }

    @PreDestroy
    public void shutdown() {
    }

    
    @PostConstruct
    public void registerAllPersistenceCapables() {

        if(packagePrefixes == null) {
            LOG.warn("Did not find key '" + PACKAGE_PREFIX_KEY + "' and so entities will not be eagerly registered in the Isis metamodel");
            return;
        }
        
        for (String packagePrefix : Splitter.on(",").split(packagePrefixes)) {
            Reflections reflections = new Reflections(packagePrefix);
            
            Set<Class<?>> entityTypes = 
                    reflections.getTypesAnnotatedWith(PersistenceCapable.class);
            
            for (Class<?> entityType : entityTypes) {
                IsisContext.getSpecificationLoader().loadSpecification(entityType);
            }
        }
    }
    
    
}
