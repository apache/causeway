package org.nakedobjects.example.ecs;

import org.nakedobjects.object.NakedObjectContext;
import org.nakedobjects.object.control.ActionAbout;
import org.nakedobjects.object.control.FieldAbout;
import org.nakedobjects.object.control.Validity;
import org.nakedobjects.object.defaults.AbstractNakedObject;
import org.nakedobjects.object.defaults.Title;
import org.nakedobjects.object.defaults.value.Date;
import org.nakedobjects.object.defaults.value.TextString;
import org.nakedobjects.object.defaults.value.Time;


/*
 * Every business object is created by subclassing AbstractNakedObject and
 * implementing the title method
 */
public class Booking extends AbstractNakedObject {
    /*
     * The version number is required for serialization, used when passing
     * objects between the client and server.
     */
    private static final long serialVersionUID = 1L;

    /*
     * The values we want the user to be able to change, via the keyboard, are
     * declared as NakedValue objects.
     */
    private final TextString reference;
    private final TextString status;
    private final Date date;
    private final Time time;

    /*
     * All the associations between the booking and the other objects are
     * declared as other NakedObject objects.
     */
    private City city;
    private Customer customer;
    private Telephone contactTelephone;
    private Location pickUp;
    private Location dropOff;
    private PaymentMethod paymentMethod;

    /*
     * The constructor is commonly used to set up the object, specifically so
     * all the value field objects are immediately avaialable.
     */
    public Booking() {
        /* Each value object is created via its default constructor. */
        reference = new TextString();
        status = new TextString();
        date = new Date();
        time = new Time();
    }

    public void aboutReference(FieldAbout about) {
        about.unmodifiable();
    }

    public void aboutStatus(FieldAbout about) {
        about.unmodifiable();
    }

    /*
     * An aboutAction... methods control the action... method that matches it's
     * name.
     */
    public void aboutActionCheckAvailability(ActionAbout about) {
        /* A ProgrammableAbout can be used to check a number of conditions. */
        //ProgrammableAbout c = new ProgrammableAbout();
        /*
         * Checks conditions and adjusts the About accordingly: if the argument
         * is false then the About is altered so that a call to canUse returns a
         * Veto.
         */
        about.unusableOnCondition(getStatus().isSameAs("Available"), "Already available");
        about.unusableOnCondition(getDate().isEmpty(), "No date specified");
    }

    public void aboutActionConfirm(ActionAbout about) {
        /*
         * This version of the makeAvailableOnCondition method also adds a
         * message to the Veto.
         */
        about.unusableOnCondition(!getStatus().isSameAs("Available"), "Status must be 'Available'");
    }

    public void aboutActionCopyBooking(ActionAbout about) {
        int sets = 0;

        sets += ((getCustomer() != null) ? 1 : 0);
        sets += ((getPickUp() != null) ? 1 : 0);
        sets += ((getDropOff() != null) ? 1 : 0);
        sets += ((getPaymentMethod() != null) ? 1 : 0);
        sets += ((getContactTelephone() != null) ? 1 : 0);

        /*
         * An About can be conditionally created: if the argument is true then
         * the returned About enables the action; if false, it disables it.
         */
        about.unusableOnCondition(sets < 3, "Not enough information to warrant copying");
    }

    public void aboutActionReturnBooking(ActionAbout about) {
        /*
         * A description can be added to the About to tell the user what the
         * action will do.
         */
        about
                .setDescription("Creates a new Booking based on the current booking.  The new booking has the pick up amd drop off locations reversed.");
        about.unusableOnCondition(!getStatus().isSameAs("Confirmed"), "Can only create a return based on a confirmed booking");

        about.unusableOnCondition(getPickUp() == null, "Pick Up location required");
        about.unusableOnCondition(getDropOff() == null, "Drop Off location required");

        /*
         * If the About is still allowing the action then the action's name will
         * be changed.
         */
        if (getPickUp() != null) {
            about.changeNameIfUsable("Return booking to " + getPickUp().title());
        }
    }

    public void aboutPickUp(FieldAbout about, Location newPickup) {
        about.setDescription("The location to pick up the customer from.");

        if ((newPickup != null) && (getCity() != null)) {
            if (newPickup.equals(getDropOff())) {
                about.unmodifiable("Pick up must differ from the drop off location");
            } else {
                boolean notInSameCity = !getCity().equals(newPickup.getCity());
                about.unmodifiableOnCondition(notInSameCity, "Location must be in " + getCity().title());
            }
        }
    }

    public void aboutDropOff(FieldAbout about, Location newDropOff) {
        about.setDescription("The location to drop the customer off at.");

        if ((newDropOff != null) && (getCity() != null)) {
            if (newDropOff.equals(getPickUp())) {
                about.unmodifiable("Drop off must differ from the Pick up location");
            } else {
                boolean notInSameCity = !getCity().equals(newDropOff.getCity());
                about.unmodifiableOnCondition(notInSameCity, "Location must be in " + getCity().title());
            }
        }
    }

    /*
     * Zero-parametered action methods are made available to the user via the
     * object's pop up menu.
     */
    public void actionCheckAvailability() {
        /* The value field is accessed and its value changed. */
        getStatus().setValue("Available");
        objectChanged();
    }

    public void actionConfirm() {
        getStatus().setValue("Confirmed");

        /*
         * The locations used are added to the customer's Locations field. Note
         * that the accessor methods are used to ensure that the objects are
         * loaded first.
         */
        getCustomer().associateLocations(getPickUp());
        getCustomer().associateLocations(getDropOff());

        if (getCustomer().getPreferredPaymentMethod() == null) {
            getCustomer().setPreferredPaymentMethod(getPaymentMethod());
        }
    }

    public Booking actionCopyBooking() {
        /*
         * A new instance is created by calling the createInstance method. This
         * ensures that the created method is always called and that the object
         * is made persistent.
         */
        Booking copiedBooking = (Booking) createInstance(Booking.class);

        copiedBooking.associateCustomer(getCustomer());
        copiedBooking.setPickUp(getPickUp());
        copiedBooking.setDropOff(getDropOff());
        copiedBooking.setPaymentMethod(getPaymentMethod());
        copiedBooking.setContactTelephone(getContactTelephone());

        /*
         * By returning the object we ensure that the user gets it: it is
         * displayed to the user in a new window.
         */
        return copiedBooking;
    }

    /*
     * One-parametered action methods are also available to the user and are
     * invoked via drag and drop.
     * 
     * When an action method is marked as static then it works for the class
     * rather than the object. To invoke this method the user must drop a
     * customer onto the Booking class icon.
     */
    public static Booking actionNewBooking(Customer customer) {
        Booking newBooking = (Booking) NakedObjectContext.getDefaultContext().createInstance(Booking.class);
        //   Booking newBooking = (Booking) createInstance(Booking.class);

        newBooking.associateCustomer(customer);
        newBooking.setPaymentMethod(customer.getPreferredPaymentMethod());

        return newBooking;
    }

    /*
     * The recommended ordering for the action methods can be specified with the
     * actionOrder method. This will affect the order of the menu items for this
     * object.
     */
    public static String actionOrder() {
        return "Check Availability, Confirm, Copy Booking, Return Booking";
    }

    public Booking actionReturnBooking() {
        Booking returnBooking = (Booking) createInstance(Booking.class);

        returnBooking.associateCustomer(getCustomer());
        returnBooking.setPickUp(getDropOff());
        returnBooking.setDropOff(getPickUp());
        returnBooking.setPaymentMethod(getPaymentMethod());
        returnBooking.setContactTelephone(getContactTelephone());

        return returnBooking;
    }

    /*
     * The associate method overrides the get/set and is called by the framework
     * instead of the ordinary accessor methods. They are used to set up complex
     * or bidirectional associations. This method delegates, to the other class,
     * the work to set up a bidirectional link.
     */
    public void associateCustomer(Customer customer) {
        customer.associateBookings(this);
    }

    public void associateDropOff(Location newDropOff) {
        setDropOff(newDropOff);
        setCity(newDropOff.getCity());
    }

    public void associatePickUp(Location newPickUp) {
        setPickUp(newPickUp);
        setCity(newPickUp.getCity());
    }

    private long createBookingRef() {
        /*
         * The object store provides the ability to create and maintain numbered
         * sequences, which are unique.
         */
        return getObjectManager().serialNumber("booking ref");
    }

    /*
     * The created method is called when the logical object is created, i.e. it
     * is not called when an object is recreated from its persisted data.
     */
    public void created() {
        status.setValue("New Booking");
        reference.setValue("#" + createBookingRef());
    }

    /*
     * The dissociate method mirrors the associate method and is called when the
     * user tries to remove a reference.
     */
    public void dissociateCustomer(Customer customer) {
        customer.dissociateBookings(this);
    }

    /*
     * The recommended order for the fields to be presented to the user can be
     * specified by the fieldOrder method.
     */
    public static String fieldOrder() {
        return "reference, status, customer, date, time, pick up, drop off, payment method";
    }

    /*
     * Each association within an object requires a get and a set method. The
     * get method simply returns the associated object's reference after it has
     * ensured that the object has been loaded into memory.
     */
    public City getCity() {
        resolve(city);

        return city;
    }

    public Telephone getContactTelephone() {
        resolve(contactTelephone);

        return contactTelephone;
    }

    public Customer getCustomer() {
        resolve(customer);

        return customer;
    }

    /*
     * Each value field only has a get method, which returns the value's
     * reference. No set is required as the value objects must be a integral
     * part of the naked object.
     */
    public final Date getDate() {
        return date;
    }

    public void validDate(Validity validity) {
        validity.cannotBeEmpty();
        Date today = new Date();
        today.today();
        validity.invalidOnCondition(date.isLessThan(today), "Booking must be in the future");
    }

    public Location getDropOff() {
        resolve(dropOff);

        return dropOff;
    }

    public PaymentMethod getPaymentMethod() {
        resolve(paymentMethod);

        return paymentMethod;
    }

    public Location getPickUp() {
        resolve(pickUp);

        return pickUp;
    }

    public final TextString getReference() {
        return reference;
    }

    public final TextString getStatus() {
        return status;
    }

    public final Time getTime() {
        return time;
    }

    /*
     * The association has a set method so a reference can be passed to the
     * object to set up the association. As the object has now changed it also
     * must be notified.
     */
    public void setCity(City newCity) {
        city = newCity;
        objectChanged();
    }

    public void setContactTelephone(Telephone newContactTelephone) {
        contactTelephone = newContactTelephone;
        objectChanged();
    }

    public void setCustomer(Customer newCustomer) {
        customer = newCustomer;
        objectChanged();
    }

    public void setDropOff(Location newDropOff) {
        dropOff = newDropOff;
        objectChanged();
    }

    public void setPaymentMethod(PaymentMethod newPaymentMethod) {
        paymentMethod = newPaymentMethod;
        objectChanged();
    }

    public void setPickUp(Location newPickUp) {
        pickUp = newPickUp;
        objectChanged();
    }

    /*
     * The title method generates a title string for the object that will be
     * used when the object is displayed. This title should identify the object
     * to the user.
     */
    public Title title() {
        /*
         * A Title object is normally retrieved from one of the object's field
         * (all Naked objects can return a Title). All of the title's methods
         * return the same Title object.
         */
        return reference.title().append(status);
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2003 Naked Objects Group
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
