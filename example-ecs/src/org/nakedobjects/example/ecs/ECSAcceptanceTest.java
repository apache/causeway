package org.nakedobjects.example.ecs;

import org.nakedobjects.object.defaults.value.Date;
import org.nakedobjects.object.defaults.value.TextString;
import org.nakedobjects.object.exploration.AbstractExplorationFixture;
import org.nakedobjects.xat.AcceptanceTestCase;
import org.nakedobjects.xat.TestObject;



public class ECSAcceptanceTest extends AcceptanceTestCase {

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(ECSAcceptanceTest.class);
    }
    
    public ECSAcceptanceTest(String name) {
        super(name);
    }


     protected void setUpFixtures() {
         addFixture(new AbstractExplorationFixture() {
             public void install() {
                 registerClass(Booking.class);
                 registerClass(City.class);
                 registerClass(Customer.class);
                 registerClass(Location.class);
                 registerClass(CreditCard.class);
                 registerClass(Telephone.class);
                 
                 createCity("Boston");
                 createCity("New York");
                 createCity("Chicago");
                 createCity("Washington");
                 createCity("Philadelphia");
                 
                 setDate(2001, 12, 14);
             }
             
             private void createCity(String name) {
                 City city = (City) createInstance(City.class);
                 city.getName().setValue(name);
             }
         } );
     }
     
    public void testBasicBooking() {
        subtitle("Set up the new booking");
        
        nextStep();

        TestObject booking = getTestClass(Booking.class.getName()).newInstance();

        nextStep("Specify the city that the booking is for");

        TestObject city = getTestClass(City.class.getName()).findInstance("New York");

        booking.associate("City", city);


        //
        nextStep("As this is for a new customer one should be specified rather than looked up");

        TestObject customer = getTestClass(Customer.class.getName()).newInstance();

        booking.associate("Customer", customer);
        customer.fieldEntry("First Name", "Richard");
        customer.fieldEntry("Last Name", "Pawson");


        //
        nextStep("Specify pick up and drop off locations");

        TestObject pickup = city.invokeAction("New Location");

        pickup.fieldEntry("Street Address", "234 E 42nd Street");
        booking.associate("Pick Up", pickup);

        TestObject dropoff = city.invokeAction("New Location");

        dropoff.fieldEntry("Street Address", "JFK Airport, BA Terminal");
        booking.associate("Drop Off", dropoff);


        //
        nextStep("Specify when to pick up.");
        booking.fieldEntry("Date", "+2");
        booking.fieldEntry("Time", "2:30:00 PM");

        booking.assertFieldContains("Date", new Date(2001, 12, 16));
        

        //
        nextStep("Specify the  payment method.");

        TestObject payment = getTestClass(CreditCard.class.getName()).newInstance();

        booking.associate("Payment Method", payment);
        payment.fieldEntry("Name On Card", "Richard W Pawson");
        payment.fieldEntry("Number", "4927834512344535");
        payment.fieldEntry("Expires", "01/04");


        //
        nextStep("Specify other details.");

        TestObject phone = getTestClass(Telephone.class.getName()).newInstance();

        booking.associate("Contact Telephone", phone);
        phone.fieldEntry("Number", "6175551234");


        //
        nextStep("Ask the customer if this is a regular number (e.g. a mobile) and if so, associate it with the customer for future use");
        phone.fieldEntry("Known As", "Mobile");
        customer.associate("Phone Numbers", phone);


        //
        nextStep("Check it is available.");
        booking.invokeAction("Check Availability");
        booking.assertFieldContains("Status", new TextString("Available"));


        //
        nextStep("And then confirm.");
        booking.invokeAction("Confirm");
        booking.assertFieldContains("Status", "Confirmed");


        //
        customer.assertFieldContains("Locations", pickup);
        customer.assertFieldContains("Locations", dropoff);
    }

    public void testReuseBooking() {
        // setup
        testBasicBooking();
        // end of setup
        
        firstStep("Retrieve the customer object.");
        
        TestObject customer = getTestClass(Customer.class.getName()).findInstance("Pawson");

        nextStep("Create a booking for this customer.");

        TestObject booking = customer.invokeAction("New Booking");

        booking.assertFieldContains("Customer", customer);
        nextStep("Retrieve the customer's home and office as the pick-up and drop-off locations.");
        booking.associate("Pick Up", 
                customer.getField("Locations", "234 E 42nd Street, New York"));
        booking.associate("Drop Off", 
                customer.getField("Locations", 
                "JFK Airport, BA Terminal, New York"));
        booking.assertFieldContains("City", "New York");
        nextStep("Use the customer's mobile phone as the contact number for this booking.");

        TestObject mobile = customer.getField("Phone Numbers", "Mobile");

        booking.associate("Contact Telephone", mobile);
        nextStep("Specify when to be picked up.");
        booking.fieldEntry("Date", "2001-12-18");
        booking.fieldEntry("Time", "7:00:00 AM");
        booking.assertFieldContains("Payment Method", 
                                 (TestObject) customer.getField(
                                         "Preferred Payment Method"));


        nextStep("Check it is available");
        booking.invokeAction("Check Availability");
        booking.assertFieldContains("Status", "Available");
        nextStep("And then confrm");
        booking.invokeAction("Confirm");
        booking.assertFieldContains("Status", "Confirmed");
    }
    
    public void testNewMethods() {
//      setup
        testBasicBooking();
        // end of setup
       
        TestObject customer = getTestClass(Customer.class.getName()).findInstance("Pawson");

        customer.assertNotEmpty("First Name");
        customer.fieldEntry("First Name", "");
        customer.assertEmpty("First Name");
        
        customer.assertFieldContains("Phone Numbers", "Mobile");
        
        customer.assertFieldContainsType("First Name", "TextString");
        customer.assertFieldContainsType("Preferred Payment Method", "CreditCard");
        customer.assertFieldContainsType(null, "Phone Numbers", "Mobile", "Telephone");
        
        customer.invokeAction("New Booking");
    }
/*
    public void story3ReturnBooking() {
        story("A Return Booking");


        //
        step("Retrieve the booking object by its reference number.");

        TestObject booking = getClassView("Bookings").findInstance("2");

        append("There are, of course, other ways of finding the original booking.  The simplest is when the customer requests the return journey when booking the original.");


        //
        step("Create the return booking which will copy the the details and transpose the pick up and drop off locations.");

        TestObject returnBooking = booking.rightClick("Return Booking");

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

        TestObject customer = getClassView("Customers").findInstance("Pawson");

        step("Retrieve the booking object by its reference number.");

        TestObject booking = customer.getField("Bookings", "#2 Confirmed");

        step("Create a copy of the booking which will copy the the details.");

        TestObject copiedBooking = booking.rightClick("Copy Booking");

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

        TestObject customer = getClassView("Customers").findInstance("Pawson");

        step("Drop one of the customer's frequently-used locations onto another to create the booking");

        TestObject pickUp = customer.getField("Locations", 
                                        "234 E 42nd Street, New York");
        TestObject dropOff = customer.getField("Locations", 
                                         "JFK Airport, BA Terminal, New York");
        TestObject booking = dropOff.drop(pickUp.drag());

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

        TestObject phone = getClassView("Telephones").newInstance();

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
*/
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
