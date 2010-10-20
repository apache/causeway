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


/**
 * 
 */
package za.co.kmz.simpleapp.dom;

import java.util.ArrayList;
import java.util.List;

import org.nakedobjects.applib.AbstractDomainObject;
import org.nakedobjects.applib.annotation.MemberOrder;

/**
 * @author Kevin
 *
 */
public class ShoppingList extends AbstractDomainObject {

	// {{ Title
    public String title() {
    	int size = getItems().size();
    	if (size == 1){
    		return "1 item";
    	} else {
    		return Integer.toString(size) + " items";
    	}
    }
    
    // {{ Items
	private List<StockItem> items = new ArrayList<StockItem>();

	@MemberOrder(sequence = "1")
	public List<StockItem> getItems() {
		return items;
	}

	public void setItems(final List<StockItem> items) {
		this.items = items;
	}
	
	public void addToItems(final StockItem stockItem) {
		// check for no-op
		if (stockItem == null || getItems().contains(stockItem)) {
			return;
		}
		// associate new
		getItems().add(stockItem);
		// additional business logic
		onAddToItems(stockItem);
	}

	public void removeFromItems(final StockItem stockItem) {
		// check for no-op
		if (stockItem == null || !getItems().contains(stockItem)) {
			return;
		}
		// dissociate existing
		getItems().remove(stockItem);
		// additional business logic
		onRemoveFromItems(stockItem);
	}

	protected void onAddToItems(final StockItem stockItem) {
	}

	protected void onRemoveFromItems(final StockItem stockItem) {
	}
	// }}


    	
}
