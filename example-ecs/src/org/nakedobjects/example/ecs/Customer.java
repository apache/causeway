package org.nakedobjects.example.ecs;

import org.nakedobjects.application.BusinessObjectContainer;
import org.nakedobjects.application.Title;
import org.nakedobjects.application.control.ActionAbout;
import org.nakedobjects.application.value.Date;
import org.nakedobjects.application.value.Percentage;
import org.nakedobjects.application.value.TextString;

import java.util.Vector;


public class Customer{
   public static String fieldOrder() {
        return "firstname, LAST name, phone numbers, locations, bOOkings";
    }

    private final Vector bookings;
    private final TextString firstName;
    private final TextString lastName;
    private final Vector locations;
    private final Vector phoneNumbers;
    private PaymentMethod preferredPaymentMethod;
    private boolean isChanged;
    private Percentage membership;
    private transient BusinessObjectContainer container;

    public Customer() {
        firstName = new TextString();
        lastName = new TextString();
        locations = new Vector();
        phoneNumbers = new Vector();
        bookings = new Vector();
        membership = new Percentage();
    }

    public void actionMethodThatFails() {
        throw new RuntimeException("This is an error created by the application");
    }
    
    public void  aboutActionCreateBooking(ActionAbout about, Location from, Location to, TextString text, Date date) {
        about.setParameter(0, "Pick up");
        about.setParameter(1, "Drop off");
        about.setParameter(3, "Date");
        
        if(! getLocations().isEmpty()) {
            about.setParameter(0, getLocations().firstElement());
        }
        
        about.setParameter(2, "Name", new TextString("#23"));
    }
    
    
    public Booking actionCreateBooking(Location from, Location to, TextString text, Date date) {
        Booking booking = (Booking) container.createInstance(Booking.class);
        booking.associateCustomer(this);
        booking.setPaymentMethod(getPreferredPaymentMethod());
        booking.setPickUp(from);
        booking.setDropOff(to);
        booking.getReference().setValue(text);
        booking.getDate().setValue(date);
        return booking;
       
    }

    public Booking actionNewBooking() {
        Booking booking = (Booking) container.createInstance(Booking.class);
        booking.associateCustomer(this);
        booking.setPaymentMethod(getPreferredPaymentMethod());
        return booking;
    }
    
    public void addToBookings(Booking booking) {
        getBookings().addElement(booking);
        markDirty();
        booking.setCustomer(this);
    }

    public void addToLocations(Location location) {
        if(! locations.contains(location)) {
            locations.addElement(location);
            markDirty();
            location.setCustomer(this);
        }
    }

    public void addToPhoneNumbers(Telephone telephone) {
        phoneNumbers.addElement(telephone);
        markDirty();
   }

    public void removeFromPhoneNumbers(Telephone telephone) {
        phoneNumbers.removeElement(telephone);
        markDirty();
   }

    public void removeFromBookings(Booking booking) {
        getBookings().removeElement(booking);
        markDirty();
       booking.setCustomer(null);
    }

    public void removeFromLocations(Location location) {
        locations.removeElement(location);
        markDirty();
     location.setCustomer(null);
    }
    
    public final Vector getBookings() {
        return bookings;
    }

    public final TextString getFirstName() {
        return firstName;
    }

    public final TextString getLastName() {
        return lastName;
    }

    public final Vector getLocations() {
        return locations;
    }
    
    public Percentage getMembership() {
        return membership;
    }

    public final Vector getPhoneNumbers() {
        return phoneNumbers;
    }

    
    public void setContainer(BusinessObjectContainer container) {
        this.container = container;
    }
    
    public PaymentMethod getPreferredPaymentMethod() {
        container.resolve(preferredPaymentMethod);

        return preferredPaymentMethod;
    }

    public void setPreferredPaymentMethod(PaymentMethod method) {
        preferredPaymentMethod = method;
        isChanged = true;
    }

    public boolean isDirty() {
        return isChanged;
    }
    
    public void markDirty() {
        isChanged = true;
    }
    
    public void clearDirty() {
        isChanged = false;
    }

    public Title title() {
        return firstName.title().append(lastName + "");
    }

    public Booking createBooking() {
        Booking newBooking = (Booking) container.createInstance(Booking.class);
        newBooking.associateCustomer(this);
        newBooking.setPaymentMethod(getPreferredPaymentMethod());
        return newBooking;
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
