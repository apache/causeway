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

package ecs.tests;

import java.util.Locale;


import org.nakedobjects.example.ecs.Booking;
import org.nakedobjects.example.ecs.City;
import org.nakedobjects.example.ecs.CreditCard;
import org.nakedobjects.example.ecs.Customer;
import org.nakedobjects.example.ecs.Location;
import org.nakedobjects.example.ecs.Telephone;
import org.nakedobjects.object.value.TextString;
import org.nakedobjects.testing.AcceptanceTest;
import org.nakedobjects.testing.DragView;
import org.nakedobjects.testing.View;
import org.nakedobjects.utility.ComponentException;
import org.nakedobjects.utility.ConfigurationException;


public class ECSStories extends AcceptanceTest {
    public ECSStories(String name) {
        super(name);
    }

    public void adminCreateCities() {
        step("Create a new instance of City for each city where the service is being offered.");

        org.nakedobjects.testing.ClassView cities = getClassView("Cities");
        View boston = cities.newInstance();

        boston.fieldEntry("Name", "Boston");
        boston.checkTitle("Boston");


        //
        step();

        View newYork = cities.newInstance();

        newYork.fieldEntry("Name", "New York");
        newYork.checkTitle("New York");


        //
        step();

        View chicago = cities.newInstance();

        chicago.fieldEntry("Name", "Chicago");
        chicago.checkTitle("Chicago");


        //
        step();

        View washington = cities.newInstance();

        washington.fieldEntry("Name", "Washington");
        washington.checkTitle("Washington");


        //
        step();

        View philadelphia = cities.newInstance();

        philadelphia.fieldEntry("Name", "Philadelphia");
        philadelphia.checkTitle("Philadelphia");
    }

    public static void main(java.lang.String[] args) throws ConfigurationException, ComponentException {
        ECSStories st = new ECSStories("ECS User Stories");

        st.start();
    }

    public void runStories() {
        story1BasicBooking();
        story2Reuse();
        story3ReturnBooking();
        story4CopyBooking();
        story5LocLoc();
    }

    public void setUp() {
    	Locale.setDefault(Locale.US);
        registerClass(Booking.class);
        registerClass(City.class);
        registerClass(Customer.class);
        registerClass(Location.class);
        registerClass(CreditCard.class);
        registerClass(Telephone.class);

        adminCreateCities();
    }

    public void story1BasicBooking() {
        story("A Basic Booking");


        //
        subtitle("Set up the new booking");
        step();

        View booking = getClassView("Bookings").newInstance();

        step("Specify the city that the booking is for");

        View city = getClassView("Cities").findInstance("New York");


        //	  city = city.selectByTitle("New York");
        booking.drop("City", city.drag());


        //
        step("As this is for a new customer one should be specified rather than looked up");

        View customer = getClassView("Customers").newInstance();

        booking.drop("Customer", customer.drag());
        customer.fieldEntry("First Name", "Richard");
        customer.fieldEntry("Last Name", "Pawson");


        //
        step("Specify pick up and drop off locations");

        View pickup = city.rightClick("New Location");

        pickup.fieldEntry("Street Address", "234 E 42nd Street");
        booking.drop("Pick Up", pickup.drag());

        View dropoff = city.rightClick("New Location");

        dropoff.fieldEntry("Street Address", "JFK Airport, BA Terminal");
        booking.drop("Drop Off", dropoff.drag());


        //
        step("Specify when to pick up.");
        booking.fieldEntry("Date", "Dec 16, 2001");
        booking.fieldEntry("Time", "2:30:00 PM");


        //
        step("Specify the  payment method.");

        View payment = getClassView("Credit Cards").newInstance();

        booking.drop("Payment Method", payment.drag());
        payment.fieldEntry("Name On Card", "Richard W Pawson");
        payment.fieldEntry("Number", "492783451234");
        payment.fieldEntry("Expires", "01.04");


        //
        step("Specify other details.");

        View phone = getClassView("Telephones").newInstance();

        booking.drop("Contact Telephone", phone.drag());
        phone.fieldEntry("Number", "6175551234");


        //
        step("Ask the customer if this is a regular number (e.g. a mobile) and if so, associate it with the customer for future use");
        phone.fieldEntry("Known As", "Mobile");
        customer.drop("Phone Numbers", phone.drag());
   //     customer.checkAssociation("Phone Numbers", phone);


        //
        step("Check it is available.");
        booking.rightClick("Check Availability");
        booking.checkField("Status", new TextString("Available"));


        //
        step("And then confirm.");
        booking.rightClick("Confirm");
        booking.checkField("Status", "Confirmed");


        //
        customer.checkField("Locations", pickup);
        customer.checkField("Locations", dropoff);
    }

    public void story2Reuse() {
        story("A booking where the previous used locations are used");
        step("Retrieve the customer object.");

        View customer = getClassView("Customers").findInstance("Pawson");

        step("Create a booking for this customer.");

        View booking = customer.rightClick("New Booking");

        booking.checkField("Customer", customer);
        step("Retrieve the customer's home and office as the pick-up and drop-off locations.");
        booking.drop("Pick Up", 
                     customer.drag("Locations", "234 E 42nd Street, New York"));
        booking.drop("Drop Off", 
                     customer.drag("Locations", 
                                   "JFK Airport, BA Terminal, New York"));
        booking.checkField("City", "New York");
        step("Use the customer's mobile phone as the contact number for this booking.");

        DragView mobile = customer.drag("Phone Numbers", "Mobile");

        booking.drop("Contact Telephone", mobile);
        step("Specify when to be picked up.");
        booking.fieldEntry("Date", "Jan  8, 2001");
        booking.fieldEntry("Time", "7:00:00 AM");
        booking.checkField("Payment Method", 
                                 (View) customer.getField(
                                         "Preferred Payment Method"));


        /*        step("Chec");
         View payment = getClassView("Credit Card").newInstance();
         booking.drop("Payment Method", payment);
         payment.fieldEntry("Name On Card", "Richard W Pawson");
         payment.fieldEntry("Number", "492783451234");
         payment.fieldEntry("Expires", "01.04");
         */
        step("Check it is available");
        booking.rightClick("Check Availability");
        booking.checkField("Status", "Available");
        step("And then confrm");
        booking.rightClick("Confirm");
        booking.checkField("Status", "Confirmed");
    }

    public void story3ReturnBooking() {
        story("A Return Booking");


        //
        step("Retrieve the booking object by its reference number.");

        View booking = getClassView("Bookings").findInstance("2");

        append("There are, of course, other ways of finding the original booking.  The simplest is when the customer requests the return journey when booking the original.");


        //
        step("Create the return booking which will copy the the details and transpose the pick up and drop off locations.");

        View returnBooking = booking.rightClick("Return Booking");

        checkFieldsIn(returnBooking);
        checkField("Customer", booking.getField("Customer"));
        checkField("Pick Up", booking.getField("Drop Off"));
        checkField("Drop Off", booking.getField("Pick Up"));
        checkField("Payment Method", booking.getField("Payment Method"));
        checkField("Contact Telephone", booking.getField("Contact Telephone"));


        //
        step("Specify when.");
        returnBooking.fieldEntry("Date", "Dec 17, 2001");
        returnBooking.fieldEntry("Time", "6:30:00 PM");


        //
        step("Check it is available");
        returnBooking.rightClick("Check Availability");
        returnBooking.checkField("Status", "Available");


        //
        step("And then confirm");
        returnBooking.rightClick("Confirm");
        returnBooking.checkField("Status", "Confirmed");
    }

    public void story4CopyBooking() {
        story("Copy a Booking from a customers previous booking");

        step("Retrieve the customer by name.");

        View customer = getClassView("Customers").findInstance("Pawson");

        step("Retrieve the booking object by its reference number.");

        View booking = customer.getField("Bookings", "#2 Confirmed");

        step("Create a copy of the booking which will copy the the details.");

        View copiedBooking = booking.rightClick("Copy Booking");

        checkFieldsIn(copiedBooking);
        checkField("Customer", booking.getField("Customer"));
        checkField("Pick Up", booking.getField("Pick Up"));
        checkField("Drop Off", booking.getField("Drop Off"));
        checkField("Payment Method", booking.getField("Payment Method"));
        checkField("Contact Telephone", booking.getField("Contact Telephone"));

        step("Specify when.");
        booking.fieldEntry("Date", "Dec 17, 2001");
        booking.fieldEntry("Time", "6:30:00 PM");


        //
        step("Check it is available");
        booking.rightClick("Check Availability");
        booking.checkField("Status", "Available");


        //
        step("And then confirm");
        booking.rightClick("Confirm");
        booking.checkField("Status", "Confirmed");
    }

    public void story5LocLoc() {
        story("A Booking based on two locations");
        step("Retrieve the customer object called Richard Pawson");

        View customer = getClassView("Customers").findInstance("Pawson");

        step("Drop one of the customer's frequently-used locations onto another to create the booking");

        View pickUp = customer.getField("Locations", 
                                        "234 E 42nd Street, New York");
        View dropOff = customer.getField("Locations", 
                                         "JFK Airport, BA Terminal, New York");
        View booking = dropOff.drop(pickUp.drag());

        step("Check that the new booking contains the right details");
        checkFieldsIn(booking);
        checkField("Customer", customer);
        checkField("Pick Up", pickUp);
        checkField("Drop Off", dropOff);
        checkField("Payment Method", 
                   customer.getField("Preferred Payment Method"));
        checkField("City", pickUp.getField("City"));
        step("Specify when to pickup");
        booking.fieldEntry("Date", "+4");
        booking.checkField("Date", booking.getField("Date").getTitle());
        booking.fieldEntry("Time", "2:30:00 PM");
        step("Specify other details");

        View phone = getClassView("Telephones").newInstance();

        booking.drop("Contact Telephone", phone.drag());
        phone.fieldEntry("Number", "6175551234");


        //
        step("Check it is available");
        booking.rightClick("Check Availability");
        booking.checkField("Status", "Available");


        //
        step("And then confirm");
        booking.rightClick("Confirm");
        booking.checkField("Status", "Confirmed");
    }
}
