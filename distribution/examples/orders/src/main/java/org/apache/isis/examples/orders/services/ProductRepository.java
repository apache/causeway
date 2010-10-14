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


package org.apache.isis.examples.orders.services;

import java.util.List;


import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.Filter;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.examples.orders.domain.Product;


@Named("Products")
public class ProductRepository extends AbstractFactoryAndRepository {

    // use ctrl+space to bring up the NO templates.

    // also, use CoffeeBytes code folding with
    // user-defined regions of {{ and }}

    // {{ showAll
    /**
     * Lists all products in the repository.
     */
    public List<Product> showAll() {
        return allInstances(Product.class);
    }

    // }}

    // {{ findByCode
    /**
     * Returns the Product with given code.
     */
    public Product findByCode(@Named("Code") final String code) {
        return firstMatch(Product.class, new Filter<Product>() {
            public boolean accept(Product obj) {
                return code.equals(obj.getCode());
            }
        });
    }

    // }}

    // {{ newProduct
    /**
     * Creates a new (already persisted) product.
     * 
     * <p>
     * For use by fixtures only.
     * 
     * @return
     */
    @Hidden
    public Product newProduct(String code, String description, int priceInPence) {
        Product product = (Product) newTransientInstance(Product.class);
        product.setCode(code);
        product.setDescription(description);
        product.setPrice(new Double(priceInPence / 100));

        getContainer().persist(product);
        return product;
    }

    // }}

    // {{ identification
    /**
     * Use <tt>Product.gif</tt> for icon.
     */
    public String iconName() {
        return "Product";
    }
    // }}

}
