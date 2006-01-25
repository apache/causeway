package bom;

import org.nakedobjects.application.BusinessObjectContainer;
import org.nakedobjects.application.Title;
import org.nakedobjects.application.control.ActionAbout;
import org.nakedobjects.application.control.FieldAbout;
import org.nakedobjects.application.control.Validity;
import org.nakedobjects.application.valueholder.Date;
import org.nakedobjects.application.valueholder.Logical;
import org.nakedobjects.application.valueholder.TextString;
import org.nakedobjects.application.valueholder.Time;


public class Booking {
    private final TextString reference;
    private final TextString status;
    private final Date date;
    private final Time time;

    private City city;
    private Customer customer;
    private Telephone contactTelephone;
    private Location pickUp;
    private Location dropOff;
    private PaymentMethod paymentMethod;
    private boolean isChanged;
    private transient BusinessObjectContainer container;

    public Booking() {
        reference = new TextString();
        status = new TextString();
        date = new Date();
        time = new Time();
    }


    public void aboutActionSave(ActionAbout about) {
        about.unusableOnCondition(date.isEmpty(), "Must have a date specified");
        about.unusableOnCondition(pickUp == null, "Must have a pick up");
        about.unusableOnCondition(dropOff == null, "Must have a drop off");
    }
    
    public void actionSave() {
        status.setValue("made persistent");
        container.makePersistent(this);
    }

    public void aboutActionReturnBooking(ActionAbout about) {
        about.setDescription(
                "Creates a new Booking based on the current booking.  The new booking has the pick up amd drop off locations reversed.");
        about.unusableOnCondition(!getStatus().isSameAs("Confirmed"), 
                                   "Can only create a return based on a confirmed booking");

        about.unusableOnCondition(getPickUp() == null, "Pick Up location required");
        about.unusableOnCondition(getDropOff() == null, "Drop Off location required");
    }

    public void aboutPickUp(FieldAbout about, Location newPickup) {
        about.setDescription("The location to pick up the customer from.");

         if ((newPickup != null) && (getCity() != null)) {
             if (newPickup.equals(getDropOff())) {
                 about.unmodifiable("Pick up must differ from the drop off location");
             } else {
                 boolean notInSameCity = ! getCity().equals(newPickup.getCity());
                 about.unmodifiableOnCondition(notInSameCity, "Location must be in " +  getCity());
             }
         }
         
         about.unmodifiableOnCondition(dropOff == null, "need drop off first");
     }

    public void aboutDropOff(FieldAbout about, Location newDropOff) {
        about.setDescription("The location to drop the customer off at.");

         if ((newDropOff != null) && (getCity() != null)) {
             if (newDropOff.equals(getPickUp())) {
                 about.unmodifiable("Drop off must differ from the Pick up location");
             } else {
                 boolean notInSameCity = ! getCity().equals(newDropOff.getCity());
                 about.unmodifiableOnCondition(notInSameCity, "Location must be in " +  getCity());
             }
         }
     }

    /* Zero-parametered action methods are made available to the user via
    * the object's pop up menu.  */
    public void actionCheckAvailability() {
        /* The value field is accessed and its value changed. */
        getStatus().setValue("Available");
        isChanged = true;
    }

    public void actionConfirm() {
        if(getPickUp() == null || getDropOff() == null) {
            throw new NullPointerException();
        }
        
        getStatus().setValue("Confirmed");

        /* The locations used are added to the customer's Locations field.
        * Note that the accessor methods are used to ensure that the objects
        * are loaded first. */
        getCustomer().addToLocations(getPickUp());
        getCustomer().addToLocations(getDropOff());

        if (getCustomer().getPreferredPaymentMethod() == null) {
            getCustomer().setPreferredPaymentMethod(getPaymentMethod());
        }
        
        isChanged = true;
    }

    public void aboutActionSimilarBooking(ActionAbout about, Location pickup, Location dropoff) {
        about.setParameter(0, "From");
        about.setParameter(1, "To");
        
  //      about.setParameter(0, getPickUp());
 //       about.setParameter(1, getDropOff());
    }
        
    public Booking actionSimilarBooking(Location pickup, Location dropoff, Logical flag) {
        Booking copiedBooking = new Booking();
        copiedBooking.setContainer(container);
        copiedBooking.created();

        copiedBooking.associateCustomer(getCustomer());
        copiedBooking.associatePickUp(pickup);
        copiedBooking.setDropOff(dropoff);
        copiedBooking.setPaymentMethod(getPaymentMethod());
        copiedBooking.setContactTelephone(getContactTelephone());
        
        if(!flag.isSet()) {
            copiedBooking.getDate().clear();
            copiedBooking.getTime().clear();
        }

        container.makePersistent(copiedBooking);
        return copiedBooking;
    }

    public Booking actionCopyBooking() {
        Booking copiedBooking = (Booking) container.createTransientInstance(Booking.class);

        if(getCustomer() != null) {
            copiedBooking.associateCustomer(getCustomer());
        }
        if(getPickUp() != null) {
        copiedBooking.setPickUp(getPickUp());
        }
        if(getDropOff() != null) {
            copiedBooking.setDropOff(getDropOff());
        }
        if(getPaymentMethod() != null) {
	        copiedBooking.setPaymentMethod(getPaymentMethod());
        }
        if(getContactTelephone() != null) {
            copiedBooking.setContactTelephone(getContactTelephone());            
        }

        /* By returning the object we ensure that the user gets it: it is 
        * displayed to the user in a new window. */
        
        container.makePersistent(copiedBooking);
        return copiedBooking;
    }

    /* One-parametered action methods are also available to the user and are
    * invoked via drag and drop.  
    * 
    * When an action method is marked as static then it works for the
    * class rather than the object.  To invoke this method the user must
    * drop a customer onto the Booking class icon. */
    public static Booking actionNewBooking(Customer customer) {
        return customer.createBooking();
    }
   
    public static Booking actionRemoteNewBookingOnServer(Customer customer) {
        return customer.createBooking();
    }

    
    public static Booking actionTestException(String name) {
        throw new RuntimeException("No way to test");
    }
    
    /* The recommended ordering for the action methods can be specified
    * with the actionOrder method.   This will affect the order of the
    * menu items for this object. */
    public static String actionOrder() {
        return "Check Availability, Confirm, Copy Booking, Return Booking";
    }

    public Booking actionReturnBooking() {
        Booking returnBooking = (Booking) container.createTransientInstance(Booking.class);

        returnBooking.associateCustomer(getCustomer());
        returnBooking.setPickUp(getDropOff());
        returnBooking.setDropOff(getPickUp());
        returnBooking.setPaymentMethod(getPaymentMethod());
        returnBooking.setContactTelephone(getContactTelephone());

        container.makePersistent(returnBooking);
        return returnBooking;
    }

    /* The associate method overrides the get/set and is called by the 
    * framework instead of the ordinary accessor methods.  They are used 
    * to set up complex or bidirectional associations.  This method delegates,
    * to the other class, the work to set up a bidirectional link.  */
    public void associateCustomer(Customer customer) {
        customer.addToBookings(this);
    }

    public void associateDropOff(Location newDropOff) {
        setDropOff(newDropOff);
        setCity(newDropOff.getCity());
    }

    public void associatePickUp(Location newPickUp) {
        if(newPickUp != null) {
	        setPickUp(newPickUp);
	        setCity(newPickUp.getCity());
        }
    }

    private long createBookingRef() {
        /* The object store provides the ability to create and maintain
        * numbered sequences, which are unique. */
       return container.serialNumber("booking ref");
    }
    
    /* The created method is called when the logical object is created, 
    * i.e. it is not called when an object is recreated from its persisted
    * data. */
    public void created() {
        status.setValue("New Booking");
        reference.setValue("#" + createBookingRef());
    }

    /* The dissociate method mirrors the associate method and is called 
    * when the user tries to remove a reference. */
    public void dissociateCustomer(Customer customer) {
        customer.removeFromBookings(this);
    }

    /* The recommended order for the fields to be presented to the user  
    * can be specified by the fieldOrder method. */
    public static String fieldOrder() {
        return "reference, status, customer, date, time, pick up, drop off, payment method";
    }

    /* Each association within an object requires a get and a set method.
     * The get method simply returns the associated object's reference after 
     * it has ensured that the object has been loaded into memory.
     */
    public City getCity() {
        container.resolve(this, city);
        return city;
    }

    public Telephone getContactTelephone() {
        container.resolve(this, contactTelephone);

        return contactTelephone;
    }

    public Customer getCustomer() {
        container.resolve(this, customer);

        return customer;
    }

    /* Each value field only has a get method, which returns the value's
     * reference.  No set is required as the value objects must be a integral
     * part of the naked object. */
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
        container.resolve(this, dropOff);

        return dropOff;
    }

    public PaymentMethod getPaymentMethod() {
        container.resolve(this, paymentMethod);

        return paymentMethod;
    }
    
    public void setContainer(BusinessObjectContainer container) {
        this.container = container;
    }
    
    public Location getPickUp() {
        container.resolve(this, pickUp);

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

    /* The association has a set method so a reference can be passed to 
     * the object to set up the association.  As the object has now changed 
     * it also must be notified.*/
    public void setCity(City newCity) {
        city = newCity;
        isChanged = true;
    }

    public void setContactTelephone(Telephone newContactTelephone) {
        contactTelephone = newContactTelephone;
        isChanged = true;
    }

    public void setCustomer(Customer newCustomer) {
        customer = newCustomer;
        isChanged = true;
    }

    public void setDropOff(Location newDropOff) {
        dropOff = newDropOff;
        isChanged = true;
    }

    public void setPaymentMethod(PaymentMethod newPaymentMethod) {
        paymentMethod = newPaymentMethod;
        isChanged = true;
    }

    public void setPickUp(Location newPickUp) {
        pickUp = newPickUp;
        isChanged = true;
    }

    public boolean isDirty() {return isChanged;}
    
    public void clearDirty() { isChanged = false;}
    
    public Title title() {
        return reference.title().append(status + "");
    }
    
    public Booking actionLocalClone() {
        Booking booking = (Booking) container.createTransientInstance(Booking.class);
        
        
        booking.setPaymentMethod(getPaymentMethod());
        
        booking.setPickUp(getPickUp().actionLocalClone());
        booking.setDropOff(getDropOff().actionLocalClone());
        booking.getReference().setValue(getReference());
        booking.getDate().setValue(getDate());
        
        container.makePersistent(booking);
        
        booking.associateCustomer(getCustomer());
        return booking;
    }
}

/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

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
