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
package org.apache.isis.testdomain.persistence.jpa;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.commons.internal.primitives._Ints;
import org.apache.isis.testdomain.jpa.entities.JpaBook;
import org.apache.isis.testdomain.jpa.entities.JpaInventory;
import org.apache.isis.testdomain.jpa.entities.JpaProduct;

import lombok.val;

final class _TestFixtures {

    static void cleanUp(RepositoryService repository) {
        repository.allInstances(JpaInventory.class).forEach(repository::remove);
        repository.allInstances(JpaProduct.class).forEach(repository::remove);
    }

    static void setUp3Books(RepositoryService repository) {

        cleanUp(repository);
        // given - expected pre condition: no inventories
        assertEquals(0, repository.allInstances(JpaInventory.class).size());
        
        // setup sample Inventory with 3 Books
        SortedSet<JpaProduct> products = new TreeSet<>();

        products.add(JpaBook.of("Sample Book-1", "A sample book for testing.", 39., "Sample Author", "ISBN-A",
                "Sample Publisher"));

        products.add(JpaBook.of("Sample Book-2", "A sample book for testing.", 29., "Sample Author", "ISBN-B",
                "Sample Publisher"));
        
        products.add(JpaBook.of("Sample Book-3", "A sample book for testing.", 99., "Sample Author", "ISBN-C",
                "Sample Publisher"));
        
        val inventory = new JpaInventory("Sample Inventory", products);
        repository.persistAndFlush(inventory);
    }
    
    static void addABookTo(JpaInventory inventory) {
        inventory.getProducts()
        .add(JpaBook.of("Sample Book-1", "A sample book for testing.", 39., "Sample Author", "ISBN-A",
                "Sample Publisher"));
    }
    
    // -- ASSERTIONS
    
    static void assertInventoryHasBooks(Collection<? extends JpaProduct> products, int...expectedBookIndices) {
        val actualBookIndices = products.stream()
                .map(JpaProduct::getName)
                .map(name->name.substring(name.length()-1))
                .mapToInt(index->_Ints.parseInt(index, 10).orElse(-1))
                .sorted()
                .toArray();
        assertArrayEquals(expectedBookIndices, actualBookIndices);
    }
    
}
