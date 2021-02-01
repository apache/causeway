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
package org.apache.isis.persistence.jdo.datanucleus.test;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        classes = {
                ConfigurationExample2.class
        },
        properties = {
//                "isis.persistence.jdo-datanucleus.impl.datanucleus.cache.level2.mode=ENABLE_SELECTIVE",
//                "isis.persistence.jdo-datanucleus.impl.datanucleus.cache.level2.type=none",
//                "isis.persistence.jdo-datanucleus.impl.datanucleus.identifier.case=MixedCase",
//                "isis.persistence.jdo-datanucleus.impl.datanucleus.persistenceByReachabilityAtCommit=false",
//                "isis.persistence.jdo-datanucleus.impl.datanucleus.schema.autoCreateAll=true",
//                "isis.persistence.jdo-datanucleus.impl.datanucleus.schema.validateAll=false",
//                "isis.persistence.jdo-datanucleus.impl.datanucleus.schema.validateConstraints=true",
//                "isis.persistence.jdo-datanucleus.impl.datanucleus.schema.validateTables=true",
//                
//                "isis.persistence.jdo-datanucleus.impl.javax.jdo.PersistenceManagerFactoryClass=org.datanucleus.api.jdo.JDOPersistenceManagerFactory",
                "spring.datasource.url=jdbc:h2:mem:test",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
       }
)
class ConfigurationTest2 {

    @Test 
    void contextLoads() {
    }
    
}
