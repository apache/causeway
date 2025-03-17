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
package org.apache.causeway.testdomain.jdo;

import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.testdomain.jdo.entities.JdoBook;
import org.apache.causeway.testdomain.jdo.entities.JdoInventory;

@Component
public class JdoInventoryDao {

    @Inject private RepositoryService repositoryService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addBook_havingIsbnA_usingRepositoryService() {
        var inventories = repositoryService.allInstances(JdoInventory.class);
        assertEquals(1, inventories.size());

        var inventory = inventories.get(0);
        assertNotNull(inventory);

        // add a conflicting book (unique ISBN violation)
        inventory.getProducts()
        .add(JdoBook.of("Sample Book-1", "A sample book for testing.", 39., "Sample Author", "ISBN-A",
                "Sample Publisher"));
    }

}
