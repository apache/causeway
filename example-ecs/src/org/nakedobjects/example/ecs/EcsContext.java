package org.nakedobjects.example.ecs;

import org.nakedobjects.object.AbstractUserContext;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.Title;
import org.nakedobjects.object.collection.InternalCollection;

public class EcsContext extends AbstractUserContext {
    private final InternalCollection cities = createInternalCollection(City.class);
    private final InternalCollection customers =createInternalCollection(Customer.class);
    
    public void created() {
		super.created();
		
		addClass(Customer.class);
		addClass(Telephone.class);
		addClass(Booking.class);
		addClass(Location.class);
		addClass(CreditCard.class);

		NakedCollection coll = getObjectManager().allInstances(City.class.getName());
		getCities().addAll(coll);
	}
    
    
	public static String singleName() {
    	return "ECS Application";
    }
    
    public Title title() {
    	return new Title("ECS Bookings").append("/", getUser());
    }
    
	public InternalCollection getCities() {
		return cities;
	}
	
	public InternalCollection getCustomers() {
		return customers;
	}
}

/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/
