package org.nakedobjects.example.ecs.exploration;

import org.nakedobjects.example.ecs.Booking;
import org.nakedobjects.example.ecs.City;
import org.nakedobjects.example.ecs.CreditCard;
import org.nakedobjects.example.ecs.Customer;
import org.nakedobjects.example.ecs.Location;
import org.nakedobjects.example.ecs.LocationGroup;
import org.nakedobjects.example.ecs.Telephone;
import org.nakedobjects.object.ApplicationContext;


public class EcsContext extends ApplicationContext {
 
    public EcsContext() {
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
