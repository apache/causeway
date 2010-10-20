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


package za.co.kmz.simpleapp.dom;

import java.util.Arrays;
import java.util.List;

import org.nakedobjects.applib.AbstractDomainObject;
import org.nakedobjects.applib.annotation.MemberOrder;
import org.nakedobjects.applib.annotation.Optional;
import org.nakedobjects.applib.value.Money;

/**
 * 
 * @author Kevin
 *
 *	A stock item is a common item that is either used in recipes, 
 *	or is managed for inventory purposes.
 * 
 *	Does not yet know how to manage e.g. butter, where 1 brick is 250g,
 *	but recipes use e.g. 100g at a time. 
 */

public class StockItem extends AbstractDomainObject {

	// {{ Title
    public String title() {
    	
        String item = getItem();
        if (item != null){
        	return item;
        } else {
        	return "Empty";
        }
    }
    // }} Title

    // {{ Item
    private String item;

	@MemberOrder(sequence = "1.1")
    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }
    // }} Name
    
    
    // {{ Units
	private String units;
	@MemberOrder(sequence = "1.2")
	public String getUnits() {
		return units;
	}
	public List<String> choicesUnits() {
		return Arrays.asList("g", "ml", "cans", "each");		
	}
	public void setUnits(final String units) {
		this.units = units;
	}
	// }}
    
    
    // Inventory
    // {{ Location
	private String location;

	@MemberOrder(sequence = "2.1")
	public String getLocation() {
		return location;
	}

	public void setLocation(final String location) {
		this.location = location;
	}
	// }}
	// {{ unOpened
	private int unOpened;

	@MemberOrder(sequence = "2.2")
	public int getUnopened() {
		return unOpened;
	}

	public void setUnopened(final int unOpened) {
		this.unOpened = unOpened;
	}
	// }}
	// {{ Opened
	private int opened;

	@MemberOrder(sequence = "2.3")
	public int getOpened() {
		return opened;
	}

	public void setOpened(final int opened) {
		this.opened = opened;
	}
	// }}
	// {{ Managed
	private boolean managed;

	@MemberOrder(sequence = "2.4")
	public boolean getManaged() {
		return managed;
	}

	public void setManaged(final boolean managed) {
		this.managed = managed;
	}
	// }}
	// Shopping List
	// {{ Price
	private Money price;

	@MemberOrder(sequence = "3.4")
	@Optional
	public Money getPrice() {
		return price;
	}

	public void setPrice(final Money price) {
		this.price = price;
	}
	// }}
}

