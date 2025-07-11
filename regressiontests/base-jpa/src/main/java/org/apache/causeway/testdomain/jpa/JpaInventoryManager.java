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
package org.apache.causeway.testdomain.jpa;

import java.util.List;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.domain.DomainObjectList.ActionDomainEvent;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.testdomain.jpa.entities.JpaProduct;

@DomainObject(nature = Nature.VIEW_MODEL)
public class JpaInventoryManager {

    @Inject private RepositoryService repository;

    // -- UPDATE PRODUCT PRICE

    public static class UpdateProductPriceEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = UpdateProductPriceEvent.class,
            choicesFrom = "allProducts")
    public JpaProduct updateProductPrice(final JpaProduct product, final double newPrice) {
        product.setPrice(newPrice);
        return product;
    }
    
    // -- WRAPPER MEMORY LEAK TESTING
    
    @Action
    public void foo() {
    }

    // -- COUNT PRODUCTS

    @Action
    public int countProducts() {
        return getAllProducts().size();
    }

    @Collection
    public List<JpaProduct> getAllProducts() {
        return repository.allInstances(JpaProduct.class);
    }

}
