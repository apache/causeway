package exploration;

import org.nakedobjects.object.ApplicationContext;

import bom.Booking;
import bom.City;
import bom.CreditCard;
import bom.Customer;
import bom.Location;
import bom.LocationGroup;
import bom.Telephone;


public class Context extends ApplicationContext {
 
    public Context() {
   //     addClass(City.class);
    //    addClass(Location.class);

        addClass(Customer.class);
        addClass(Booking.class);
        addClass(Location.class);
        addClass(Telephone.class);
        addClass(CreditCard.class);
        addClass(City.class);
        addClass(LocationGroup.class);
    }

    public String name() {
        return "ECS Bookings";
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */
