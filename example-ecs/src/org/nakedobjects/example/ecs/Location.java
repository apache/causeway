package org.nakedobjects.example.ecs;

import org.nakedobjects.object.AbstractNakedObject;
import org.nakedobjects.object.Title;
import org.nakedobjects.object.control.ActionAbout;
import org.nakedobjects.object.value.TextString;


public class Location extends AbstractNakedObject {
    private static final long serialVersionUID = 1L;
    private final TextString streetAddress;
    private final TextString knownAs;
    private City city;
    private Customer customer;

    public Location() {
        streetAddress = new TextString();
        knownAs = new TextString();
    }

    public void aboutActionNewBooking(ActionAbout about, Location location) {
        about.setDescription(
                "Giving one location to another location creates a new booking going from the given location to the recieving location.");
        about.unusableOnCondition(equals(location), "Two different locations are required");

        boolean sameCity = getCity() != null && location != null && getCity().equals(location.getCity());

        about.unusableOnCondition(! sameCity, "Locations must be in the same city");
        about.changeNameIfUsable("New booking from " + Title.title(location) + 
                                " to " + title());
    }

    public Booking actionNewBooking(Location location) {
        Booking booking = (Booking) createInstance(Booking.class);
        Customer customer = location.getCustomer();

        booking.setPickUp(location);
        booking.setDropOff(this);

        if (customer != null) {
            booking.setCustomer(customer);
            booking.setPaymentMethod(customer.getPreferredPaymentMethod());
        }

        booking.setCity(location.getCity());

        return booking;
    }

    public City getCity() {
        resolve(city);

        return city;
    }

    public Customer getCustomer() {
        resolve(customer);

        return customer;
    }

    public final TextString getKnownAs() {
        return knownAs;
    }

    public final TextString getStreetAddress() {
        return streetAddress;
    }

    public void setCity(City newCity) {
        city = newCity;
        objectChanged();
    }

    public void setCustomer(Customer newCustomer) {
        customer = newCustomer;
        objectChanged();
    }

    public Title title() {
        if (knownAs.isEmpty()) {
            return streetAddress.title().append(",", getCity());
        } else {
            return knownAs.title().append(",", getCity());
        }
    }
}

/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2003  Naked Objects Group Ltd

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
