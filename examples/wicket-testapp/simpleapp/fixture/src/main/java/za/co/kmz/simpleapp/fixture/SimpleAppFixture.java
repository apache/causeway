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


package za.co.kmz.simpleapp.fixture;

import org.nakedobjects.applib.fixtures.AbstractFixture;

import za.co.kmz.simpleapp.dom.ShoppingList;
import za.co.kmz.simpleapp.dom.StockItem;
import za.co.kmz.simpleapp.service.StockItemRepositoryInterface;



public class SimpleAppFixture extends AbstractFixture {

	// {{ injected dependencies
	// {{ injected: StockItemRepository
	private StockItemRepositoryInterface stockRepository;
	public void setStockItemRepository(final StockItemRepositoryInterface stockRepository) {
		this.stockRepository = stockRepository;
	}
	// }}

	// }}


	
    @Override
    public void install() {
    	// Create some stock
        StockItem butter = newStockItem("Butter", "Dairy", "g");
        StockItem flour = newStockItem("Flour", "Baking", "g");
        
        // Create a list with a single item
        ShoppingList list = stockRepository.newShoppingList();
        list.addToItems(butter);
        
        ShoppingList list2 = stockRepository.newShoppingList();
        list2.addToItems(butter);
        list2.addToItems(flour);
        
        StockItem item;
        ShoppingList list3 = stockRepository.newShoppingList();
        for (int i = 0; i < 16; i++){
        	item = newStockItem("Item "+Integer.toString(i), "Other", "each");
        	list3.addToItems(item);
        }
    }
    
    private StockItem newStockItem(String name, String category, String unit){
    	StockItem item = this.stockRepository.newStockItem();
    	item.setItem(name);
    	item.setUnits(unit);
        persist(item);
    	return item;
    }
}
