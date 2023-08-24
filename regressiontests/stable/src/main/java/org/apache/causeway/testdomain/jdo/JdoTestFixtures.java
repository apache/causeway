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

import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.testdomain.fixtures.EntityTestFixtures;
import org.apache.causeway.testdomain.fixtures.InventoryJaxbVm;
import org.apache.causeway.testdomain.jdo.entities.JdoBook;
import org.apache.causeway.testdomain.jdo.entities.JdoInventory;
import org.apache.causeway.testdomain.jdo.entities.JdoProduct;
import org.apache.causeway.testdomain.util.dto.BookDto;
import org.apache.causeway.testdomain.util.dto.IBook;

import lombok.val;

@Service
public class JdoTestFixtures extends EntityTestFixtures {

    public void addABookTo(final JdoInventory inventory) {
        inventory.getProducts()
        .add(JdoBook.of("Sample Book-1", "A sample book for testing.", 39., "Sample Author", "ISBN-A",
                "Sample Publisher"));
    }

    @Override
    protected Class<? extends InventoryJaxbVm<? extends IBook>> vmClass() {
        return JdoInventoryJaxbVm.class;
    }

    // -- HELPER

    @Override
    public void clearRepository() {
        repository.allInstances(JdoInventory.class).forEach(repository::remove);
        repository.allInstances(JdoBook.class).forEach(repository::remove);
        repository.allInstances(JdoProduct.class).forEach(repository::remove);
    }

    @Override
    public void add3Books() {

        // given - expected pre condition: no inventories
        assertEquals(0, repository.allInstances(JdoInventory.class).size());

        // setup sample Inventory with 3 Books
        SortedSet<JdoProduct> products = new TreeSet<>();

        BookDto.samples()
        .map(JdoBook::fromDto)
        .forEach(products::add);

        val inventory = JdoInventory.of("Sample Inventory", products);
        repository.persistAndFlush(inventory);
    }

    @Override
    protected void initSchema() {
        interactionService.runAnonymous(()->{
            repository.allInstances(JdoInventory.class);
        });
    }

}
