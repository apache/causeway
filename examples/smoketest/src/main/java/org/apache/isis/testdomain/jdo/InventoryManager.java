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

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ViewModel;
import org.apache.isis.applib.domain.DomainObjectList.ActionDomainEvent;

@ViewModel
public class InventoryManager {

	public static class UpdateProductPriceEvent extends ActionDomainEvent {
		private static final long serialVersionUID = 1L;}
	
    @Action(domainEvent = UpdateProductPriceEvent.class)
    public Product updateProductPrice(Product product, double newPrice) {
        product.setPrice(newPrice);
        return product;
    }
    
}
