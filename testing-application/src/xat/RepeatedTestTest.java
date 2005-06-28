package xat;

import org.nakedobjects.application.system.ExplorationClock;
import org.nakedobjects.example.xat.JavaAcceptanceTestCase;
import org.nakedobjects.reflector.java.fixture.JavaFixture;
import org.nakedobjects.utility.Profiler;

import java.util.Locale;

import junit.framework.TestSuite;
import bom.Booking;
import bom.City;
import bom.CreditCard;
import bom.Customer;
import bom.Location;
import bom.Telephone;



public class RepeatedTestTest extends JavaAcceptanceTestCase {
    static Profiler profiler= new Profiler("");
    
    static RepeatedTestTest tests[] = new RepeatedTestTest[20];

 //   private static int next = 0;
    
    public static void main(java.lang.String[] args) {
        for (int j = 0; j < 1; j++) {
            
            TestSuite suite = new TestSuite("xxxt");
            for (int i = 0; i < 10; i++) {
                suite.addTestSuite(RepeatedTestTest.class);
            }
            junit.textui.TestRunner.run(suite);
        }
        
    }
    
    public RepeatedTestTest(String name) {
        super(name);
        
  //      tests[next++] = this;
    }


     protected void setUpFixtures() {
         addFixture(new JavaFixture() {
             public void install() {
                 System.out.println("\ninstall fixture entry \t" +  Profiler.memoryLog());

                 profiler.reset();
                 profiler.start();

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
                 
                 
                 for (int i = 0; i < 100; i++) {
                     Customer c = (Customer) createInstance(Customer.class);
                     Location l = (Location) createInstance(Location.class);
                     l.getKnownAs().setValue("ID " + new java.util.Date());
                     l.getStreetAddress().setValue(i + " Main St.");
                     c.getFirstName().setValue("Harry");
                     c.addToLocations(l);
                 }                 
                 profiler.stop();
                 System.out.println("install fixture exit \t" + profiler.timeLog() + " \t" + Profiler.memoryLog());
                 profiler.reset();
                 profiler.start();
                 
             }
             
             private void createCity(String name) {
                 City city = (City) createInstance(City.class);
                 city.setName(name);
             }
         } );
     }
       
    public void testCreateBooking() {
        profiler.stop();
        System.out.println("time between end of install fixture and start of test\t" + profiler.timeLog() + " \t" + Profiler.memoryLog());

 //       System.gc();
  //      System.runFinalization();
        try {
            Thread.sleep(
                    0000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        profiler.reset();
        profiler.start();
/*
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
        */
        profiler.stop();
        System.out.println("test \t" + profiler.timeLog() + " \t" + Profiler.memoryLog());

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
