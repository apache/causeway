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


package za.co.kmz.simpleapp.service;

import java.util.ArrayList;
import java.util.List;

import org.nakedobjects.applib.AbstractFactoryAndRepository;
import org.nakedobjects.applib.annotation.Named;

import za.co.kmz.simpleapp.dom.ShoppingList;
import za.co.kmz.simpleapp.dom.StockItem;
import za.co.kmz.simpleapp.service.StockItemRepositoryInterface;


public class StockItemRepository extends AbstractFactoryAndRepository implements StockItemRepositoryInterface {

    public String title() {
        return "Stock Items";
    }

    public String iconName() {
        return "StockItem";
    }

    public List<StockItem> allStockItems() {
        return allInstances(StockItem.class);
    }

    public StockItem newStockItem() {
        StockItem object = newTransientInstance(StockItem.class);
        return object;
    }

	public StockItem findStockItem(@Named("Name") String title) {
		StockItem item = this.firstMatch(StockItem.class, title);
		return item;
	}
	
    public StockItem newStockItem(String name, String category, String unit){
    	StockItem item = newStockItem();
    	item.setItem(name);
    	item.setUnits(unit);
        persist(item);
    	return item;
    }
	

	// Shopping lists
	public ShoppingList newShoppingList() {
		ShoppingList list = newPersistentInstance(ShoppingList.class);
		return list;
	}
	public List<ShoppingList> allShoppingLists() {
        return allInstances(ShoppingList.class);
	}

	public List<ShoppingList> allShoppingListsContaining(StockItem item) {
		List<ShoppingList> lists = allShoppingLists();
		if (lists.size() == 0){
			return null;
		}
		List<ShoppingList> retList = new ArrayList<ShoppingList>();
		for (ShoppingList list : lists) {
			if (list.getItems().contains(item)){
				retList.add(list);
			}
		}
		if (retList.size() > 0){
			return retList;
		}
		return null;
	}

	
}
