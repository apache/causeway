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


import junit.framework.TestSuite;

import junit.textui.TestRunner;

import org.nakedobjects.example.ecs.*;
import org.nakedobjects.testing.NakedTestCase;
import org.nakedobjects.testing.View;


public class ECSUnitTests extends NakedTestCase {
    public ECSUnitTests(String name) {
        super(name);
        init();
    }

    public static void main(java.lang.String[] args) {
        TestRunner.run(new TestSuite(ECSUnitTests.class));
    }

    protected void setUp() {
        registerClass(Booking.class);
        registerClass(City.class);
        registerClass(Customer.class);
        registerClass(Location.class);
        registerClass(CreditCard.class);
    }

    public void testBooking() {
        View booking = getClassView("Bookings").newInstance();

        booking.assertFieldReadOnly("Reference");
        booking.assertFieldReadOnly("Status");

        booking.assertFieldContains("Status", "New Booking");

        booking.assertCantRightClick("Confirm");

        booking.assertFieldContains("Customer", (View) null);
        booking.assertFieldContains("City", (View) null);
        booking.assertFieldContains("Pick Up", (View) null);
        booking.assertFieldContains("Drop Off", (View) null);
    }

    public void testBooking2() {
        View booking = getClassView("Bookings").newInstance();

        booking.assertFieldReadOnly("Reference");
        booking.assertFieldReadOnly("Status");

        View city = getClassView("Cities").newInstance();

        booking.testField("City", city);

        booking.assertFieldContains("Status", "New Booking");
        booking.assertCantRightClick("Confirm");


			booking.rightClick("Check Availability");
        booking.assertFieldContains("Status", "Available");
	
        booking.rightClick("Confirm");

        booking.assertTitleEquals("title is a reference number", booking.getField("Reference").getTitle() + " Confirmed");
    }

    public void testBookingForCustomer1() {
        // done conventionally
        Booking booking = new Booking();
        Customer customer = new Customer();

        booking.associateCustomer(customer);

        assertEquals(customer, booking.getCustomer());
        assertTrue(customer.getBookings().contains(booking));

        booking.actionCheckAvailability();
        assertEquals("Available", booking.getStatus().title().toString());
    }

    public void testBookingForCustomer2() {
        // using views
        View booking = getClassView("Bookings").newInstance();
        View customer = getClassView("Customers").newInstance();

        booking.drop("Customer", customer.drag());

        booking.assertFieldContains("Customer", customer);
        customer.assertFieldContains("Bookings", booking);

        booking.rightClick("Check Availability");
        booking.assertFieldContains("Status", "Available");
    }

    public void testBookingLocations() {
        // standard junit way
        Booking bk = new Booking();
        Location a = new Location();
        Location b = new Location();

        bk.associatePickUp(a);
        bk.associateDropOff(b);

        assertEquals("pick up", a, bk.getPickUp());
        assertEquals("city copied", a.getCity(), bk.getCity());
        assertEquals("drop off", b, bk.getDropOff());

        // alternatively
        View booking = getClassView("Bookings").newInstance();
        View locationA = getClassView("Locations").newInstance();
        View locationB = getClassView("Locations").newInstance();

        booking.drop("Pick Up", locationA.drag());
        booking.drop("Drop Off", locationB.drag());

        booking.assertFieldContains("Pick Up", locationA);
        booking.assertFieldContains("Drop Off", locationB);
    }

    public void testCity() {
        View city = getClassView("Cities").newInstance();

        city.testField("Name", "Boston");
        city.assertTitleEquals(city.getFieldTitle("Name"));
    }

    public void testCreditCard() {
        View payment = getClassView("Credit Cards").newInstance();

        payment.fieldEntry("Name On Card", "Richard W Pawson");
        payment.fieldEntry("Number", "492783451234");
        payment.fieldEntry("Expires", "01.04");
        payment.assertTitleEquals("*******51234");
    }

    public void testCustomer() {
        View customer = getClassView("Customers").newInstance();

        customer.fieldEntry("First Name", "Richard");
        customer.fieldEntry("Last Name", "Pawson");
        customer.assertTitleEquals("Richard Pawson");
    }

    public void testLocation() {
        View location = getClassView("Locations").newInstance();

        location.assertTitleEquals("Title is intially blank", "");
        location.fieldEntry("Street Address", "175 Reading Road");
        location.assertTitleEquals("Title picks up street address", 
                                   location.getField("Street Address")
                                           .getTitle());
        location.fieldEntry("Known As", "Henley");
        location.assertTitleEquals("Title picks up 'Known as'", 
                                   location.getField("Known As").getTitle());

        View city = getClassView("Cities").newInstance();

        location.drop("City", city.drag());
        location.assertFieldContains("City", city);
    }
}
