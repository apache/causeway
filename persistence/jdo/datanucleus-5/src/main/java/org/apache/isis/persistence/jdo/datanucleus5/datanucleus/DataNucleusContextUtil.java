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
package org.apache.isis.persistence.jdo.datanucleus5.datanucleus;

import java.util.Map;
import java.util.Optional;

import org.datanucleus.ExecutionContext;

import org.apache.isis.core.metamodel.context.MetaModelContext;

import lombok.val;
import lombok.experimental.UtilityClass;

/**
 * 
 * @since 2.0
 *
 */
@UtilityClass
public class DataNucleusContextUtil {
    
    // required to be lower-case for DN to be accepted
    private final static String METAMODELCONTEXT_PROPERTY_KEY = "isis.metamodelcontext"; 

    public static void putMetaModelContext(
            Map<String, Object> map, 
            MetaModelContext metaModelContext) {

        map.put(METAMODELCONTEXT_PROPERTY_KEY, metaModelContext);
    }
    
    public static Optional<MetaModelContext> extractMetaModelContext(ExecutionContext ec) {

        val metaModelContext = (MetaModelContext) ec.getNucleusContext()
                .getConfiguration()
                .getPersistenceProperties()
                .get(METAMODELCONTEXT_PROPERTY_KEY);
        
        return Optional.ofNullable(metaModelContext);
    }
    
}
