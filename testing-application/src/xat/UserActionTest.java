package xat;

import org.nakedobjects.application.system.ExplorationClock;
import org.nakedobjects.application.valueholder.Date;
import org.nakedobjects.application.valueholder.Time;
import org.nakedobjects.example.xat.JavaAcceptanceTestCase;
import org.nakedobjects.reflector.java.fixture.JavaFixture;
import org.nakedobjects.xat.TestObject;

import java.util.Locale;

import bom.Booking;
import bom.City;
import bom.CreditCard;
import bom.Customer;
import bom.Location;
import bom.Telephone;



public class UserActionTest extends JavaAcceptanceTestCase {

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(UserActionTest.class);
    }
    
    public UserActionTest(String name) {
        super(name);
    }


     protected void setUpFixtures() {
         addFixture(new JavaFixture() {
             public void install() {

                 Locale.setDefault(Locale.FRANCE);
                 
                 ExplorationClock clock = new ExplorationClock();
                 
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
                 
                 clock.setDate(2001, 12, 14);
                 
                 
                 
                 Customer c = (Customer) createInstance(Customer.class);
                 Location l = (Location) createInstance(Location.class);
                 
                 c.getFirstName().setValue("Harry");
                 c.addToLocations(l);
             }
             
             private void createCity(String name) {
                 City city = (City) createInstance(City.class);
                 city.getName().setValue(name);
             }
         } );
     }
     
     public void testInstantiate() {
    
         long clock = System.currentTimeMillis();
         for (int i = 0; i < 2000; i++) {
            createBooking();
            if(i > 0 && i % 20 == 0) {
                System.out.println(i + " " + (System.currentTimeMillis() - clock) / 1000.0);
                clock = System.currentTimeMillis();
            }
        }
    }
       
    public void createBooking() {
        TestObject booking = getTestClass(Booking.class.getName()).newInstance();
        TestObject city = getTestClass(City.class.getName()).findInstance("New York");
        booking.associate("City", city);
        TestObject customer = getTestClass(Customer.class.getName()).newInstance();

        booking.associate("Customer", customer);
        customer.fieldEntry("First Name", "Richard");
        customer.fieldEntry("Last Name", "Pawson");

        TestObject pickup = city.invokeActionReturnObject("New Location", NO_PARAMETERS);
        pickup.fieldEntry("Street Address", "234 E 42nd Street");
        booking.associate("Pick Up", pickup);
        
        TestObject dropoff = city.invokeActionReturnObject("New Location", NO_PARAMETERS);
        dropoff.fieldEntry("Street Address", "JFK Airport, BA Terminal");
        booking.associate("Drop Off", dropoff);

        booking.fieldEntry("Date", "+2");
        booking.fieldEntry("Time", "14:30:00 PM");

        booking.assertFieldContains("Time", new Time(14, 30));
        booking.assertFieldContains("Date", new Date(2001, 12, 16));

        TestObject payment = getTestClass(CreditCard.class.getName()).newInstance();
        booking.associate("Payment Method", payment);
        payment.fieldEntry("Name On Card", "Richard W Pawson");
        payment.fieldEntry("Number", "4927834512344535");
        payment.fieldEntry("Expires", "01/04");

        TestObject phone = getTestClass(Telephone.class.getName()).newInstance();
        booking.associate("Contact Telephone", phone);
        phone.fieldEntry("Number", "6175551234");

        phone.fieldEntry("Known As", "Mobile");
        customer.associate("Phone Numbers", phone);

        booking.invokeAction("Check Availability", NO_PARAMETERS);
        booking.assertFieldContains("Status", "Available");

        booking.invokeAction("Confirm", NO_PARAMETERS);
        booking.assertFieldContains("Status", "Confirmed");

        customer.assertFieldContains("Locations", pickup);
        customer.assertFieldContains("Locations", dropoff);
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
