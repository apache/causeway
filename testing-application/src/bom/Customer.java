package bom;

import org.nakedobjects.application.ApplicationException;
import org.nakedobjects.application.BusinessObjectContainer;
import org.nakedobjects.application.Title;
import org.nakedobjects.application.control.ActionAbout;
import org.nakedobjects.application.valueholder.Date;
import org.nakedobjects.application.valueholder.MultilineTextString;
import org.nakedobjects.application.valueholder.Password;
import org.nakedobjects.application.valueholder.Percentage;
import org.nakedobjects.application.valueholder.TextString;
import org.nakedobjects.application.valueholder.WholeNumber;
import org.nakedobjects.object.persistence.ConcurrencyException;

import java.net.SocketException;
import java.util.Vector;

import org.apache.log4j.Logger;


public class Customer {
    public static String fieldOrder() {
        return "firstname, LAST name, phone numbers, locations, bOOkings";
    }

    private final Vector bookings;
    private transient BusinessObjectContainer container;
    private final TextString firstName;
    private boolean isChanged;
    private final TextString lastName;
    private final Vector locations;
    private Percentage membership;
    private final Vector phoneNumbers;
    private PaymentMethod preferredPaymentMethod;
    private final Vector PaymentMethods = new Vector();
    private final Password password = new Password(8);
    private MultilineTextString notes = new MultilineTextString();

    public Password getPassword() {
        return password;
    }
    
    public MultilineTextString getNotes() {
        return notes;
    }

    public Vector getPaymentMethods() {
        return PaymentMethods;
    }

    public void addToPaymentMethods(PaymentMethod method) {}

    public void removeFromPaymentMethods(PaymentMethod method) {}

    public Customer() {
        firstName = new TextString();
        lastName = new TextString();
        locations = new Vector();
        phoneNumbers = new Vector();
        bookings = new Vector();
        membership = new Percentage();
    }
    

    public void aboutActionTestInput(ActionAbout about, WholeNumber number) {
        about.setParameter(0, "number");
        about.setParameter(0, new WholeNumber(10));
    }
    
    public void actionTestInput(WholeNumber number) {
        
    }

    public void actionUseAllInstances() {
        container.allInstances(Location.class);
    }

    public Vector actionNewLocations() {
        LocationCollection collection = new LocationCollection();
        collection.addAll(locations);
        return collection;
    }

    public void actionInvokeLocationMethodOnOneOfTheBookings() {
        Booking booking = (Booking) getBookings().elementAt(0);
        Location pickUp = booking.getPickUp();
        pickUp.isLondon();
    }

    public void aboutActionCreateBooking(
            ActionAbout about,
            Location from,
            Location to,
            Telephone telephone,
            TextString text,
            Date date,
            Date returning) {
        about.setParameter(0, "Pick up");
        about.setParameter(1, "Drop off");
        about.setParameter(3, "Date");
        about.setParameter(4, "Return on");

        /*
         * if (!getLocations().isEmpty()) { about.setParameter(0, getLocations().firstElement()); }
         */
        text.toString();
        date.toString();

        about.setParameter(3, "Name", new TextString("#23"));
        //    about.setParameter(3, (Object) "hsadaskll");

        about.setDescription("From " + from + " to " + to + ", call on " + telephone);

        about.unusableOnCondition(from == null, "must have a from location");
        about.unusableOnCondition(text.isEmpty(), "Need some text");
        about.unusableOnCondition(date.isLessThanOrEqualTo(new Date()), "Date must be tommorow or after");
        about.unusableOnCondition(returning.isLessThanOrEqualTo(new Date()), "Date must be tommorow or after");
    }

    public Booking actionCreateBooking(Location from, Location to, Telephone telephone, TextString text, Date date, Date returnon) {
        Booking booking = (Booking) container.createInstance(Booking.class);
        booking.associateCustomer(this);
        booking.setPaymentMethod(getPreferredPaymentMethod());
        booking.setPickUp(from);
        booking.setDropOff(to);
        booking.getReference().setValue(text);
        booking.getDate().setValue(date);
        return booking;

    }

    public Vector actionLocations() {
        Vector v = new Vector();
        for (int i = 0; i < locations.size(); i++) {
            v.addElement(locations.elementAt(i));
        }
        return v;
    }

    public LocationVector actionLocationsAsVector() {
        LocationVector v = new LocationVector();
        for (int i = 0; i < locations.size(); i++) {
            v.addElement(locations.elementAt(i));
        }
        return v;
    }

    public Booking actionUsePaymentMethod(PaymentMethod method) {
        Booking booking = (Booking) container.createInstance(Booking.class);
        booking.associateCustomer(this);
        booking.setPaymentMethod(method);
        return booking;
    }

    public void actionShowFailureOfSystem() throws SocketException {
        throw new SocketException("example exception for system failure");
    }

    public void actionShowRuntimeException() {
        throw new NullPointerException("example runtime exception");
    }

    public void actionConcurrencyException() {
        throw new ConcurrencyException("Another user has changed the object X");
    }

    public void actionFailureOfApplication() {
        throw new ApplicationException("This is an error created by the application", new RuntimeException(
                "This is an error created by the application"));
    }

    public Booking actionNewBooking() {
        Booking booking = (Booking) container.createInstance(Booking.class);
        booking.associateCustomer(this);
        booking.setPaymentMethod(getPreferredPaymentMethod());
        return booking;
    }

    public void actionNewBookingNoReturn() {
        Booking booking = (Booking) container.createInstance(Booking.class);
        booking.associateCustomer(this);
        booking.setPaymentMethod(getPreferredPaymentMethod());
    }

    public Location actionNewLocation() {
        Location l = new Location();
        l.setContainer(container);

        container.makePersistent(l);

        addToLocations(l);

        return l;
    }

    public void addToBookings(Booking booking) {
        getBookings().addElement(booking);
        markDirty();
        booking.setCustomer(this);
    }

    public void addToLocations(Location location) {
        if (!locations.contains(location)) {
            locations.addElement(location);
            markDirty();
            location.setCustomer(this);
        }
    }

    public void addToPhoneNumbers(Telephone telephone) {
        phoneNumbers.addElement(telephone);
        markDirty();
    }

    public void clearDirty() {
        isChanged = false;
    }

    Booking createBooking() {
        Booking newBooking = (Booking) container.createInstance(Booking.class);
        newBooking.associateCustomer(this);
        newBooking.setPaymentMethod(getPreferredPaymentMethod());
        return newBooking;
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

    public PaymentMethod getPreferredPaymentMethod() {
        container.resolve(this, preferredPaymentMethod);

        return preferredPaymentMethod;
    }

    public boolean isDirty() {
        return isChanged;
    }

    public void markDirty() {
        isChanged = true;
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

    public void removeFromPhoneNumbers(Telephone telephone) {
        phoneNumbers.removeElement(telephone);
        markDirty();
    }

    public void setContainer(BusinessObjectContainer container) {
        this.container = container;
    }

    public void setPreferredPaymentMethod(PaymentMethod method) {
        preferredPaymentMethod = method;
        isChanged = true;
    }

    public Title title() {
        return firstName.title().append(lastName + "");
    }

    protected void finalize() throws Throwable {
        super.finalize();
        Logger.getLogger("Customer").info("finalizing customer");
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */
