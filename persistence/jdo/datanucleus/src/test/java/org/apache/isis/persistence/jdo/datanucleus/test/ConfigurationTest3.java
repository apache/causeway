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

import javax.inject.Inject;
import javax.inject.Provider;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.val;

@SpringBootTest(
        classes = {
                ConfigurationExample3.class
        },
        properties = {
//                XXX not used with this example
//                "spring.datasource.url=jdbc:h2:mem:test",
//                "spring.datasource.driver-class-name=org.h2.Driver",
//                "spring.datasource.username=sa",
//                "spring.datasource.password=",
       }
)
class ConfigurationTest3 {

    @Inject
    private Provider<ConfigurationExample3.ExampleDao> exampleDaoProvider;

    @Test
    void contextLoads() {

        assertNotNull(exampleDaoProvider);

        val dao = exampleDaoProvider.get();
        assertNotNull(dao);

        val pmf = dao.getPersistenceManagerFactory();
        assertNotNull(pmf);

        assertTrue(pmf.getClass().getName().contains("Proxy"));

    }

}
