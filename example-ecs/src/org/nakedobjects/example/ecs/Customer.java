package org.nakedobjects.example.ecs;

import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.defaults.AbstractNakedObject;
import org.nakedobjects.object.defaults.Title;
import org.nakedobjects.object.defaults.value.TextString;


public class Customer extends AbstractNakedObject {
    private static final long serialVersionUID = 1L;
    private final TextString lastName;
    private final TextString firstName;
    private final InternalCollection locations;
    private final InternalCollection phoneNumbers;
    private final InternalCollection bookings;
    private PaymentMethod preferredPaymentMethod;

    public Customer() {
        firstName = new TextString();
        lastName = new TextString();
        locations = createInternalCollection(Location.class);
        phoneNumbers = createInternalCollection(Telephone.class);
        bookings = createInternalCollection(Booking.class);
    }

    public Booking actionNewBooking() {
        Booking b = (Booking) createInstance(Booking.class);

        b.associateCustomer(this);
        b.setPaymentMethod(this.getPreferredPaymentMethod());

        return b;
    }

    public void associateBookings(Booking booking) {
        getBookings().add(booking);
        booking.setCustomer(this);
    }

    public void associateLocations(Location location) {
        locations.add(location);
        location.setCustomer(this);
    }

    public void dissociateBookings(Booking booking) {
        getBookings().remove(booking);
        booking.setCustomer(null);
    }

    public void dissociateLocations(Location location) {
        locations.remove(location);
        location.setCustomer(null);
    }

    public static String fieldOrder() {
        return "first name, last name, phone numbers, bookings";
    }

    public final InternalCollection getBookings() {
        return bookings;
    }

    public final TextString getFirstName() {
        return firstName;
    }

    public final TextString getLastName() {
        return lastName;
    }

    public final InternalCollection getLocations() {
        return locations;
    }

    public final InternalCollection getPhoneNumbers() {
        return phoneNumbers;
    }

    public PaymentMethod getPreferredPaymentMethod() {
        resolve(preferredPaymentMethod);

        return preferredPaymentMethod;
    }

    public void setPreferredPaymentMethod(PaymentMethod method) {
        preferredPaymentMethod = method;
        objectChanged();
    }

    public Title title() {
        return firstName.title().append(lastName);
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
