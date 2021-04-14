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
package org.apache.isis.testdomain.jdo;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.domain.DomainObjectList.ActionDomainEvent;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.testdomain.jdo.entities.JdoProduct;

@DomainObject(nature = Nature.VIEW_MODEL)
public class JdoInventoryManager {

    @Inject private RepositoryService repository;
    
    // -- UPDATE PRODUCT PRICE

    public static class UpdateProductPriceEvent extends ActionDomainEvent {}

    @Action(domainEvent = UpdateProductPriceEvent.class, executionPublishing = Publishing.ENABLED)
    public JdoProduct updateProductPrice(JdoProduct product, double newPrice) {
        product.setPrice(newPrice);
        return product;
    }

    // -- COUNT PRODUCTS

    @Action
    public int countProducts() {
        return listAllProducts().size();
    }
    
    @Action
    public List<JdoProduct> listAllProducts() {
        return repository.allInstances(JdoProduct.class);
    }

    

}
