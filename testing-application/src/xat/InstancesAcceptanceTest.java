package xat;

import org.nakedobjects.application.system.ExplorationClock;
import org.nakedobjects.example.xat.JavaAcceptanceTestCase;
import org.nakedobjects.reflector.java.fixture.JavaFixture;
import org.nakedobjects.xat.TestClass;
import org.nakedobjects.xat.TestObject;

import java.util.Locale;

import bom.Booking;
import bom.City;
import bom.CreditCard;
import bom.Customer;
import bom.Location;
import bom.Telephone;



public class InstancesAcceptanceTest extends JavaAcceptanceTestCase {
 //S    private Customer c;

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(InstancesAcceptanceTest.class);
    }
    
    public InstancesAcceptanceTest(String name) {
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
                 City city = createCity("Chicago");
                 createCity("Washington");
                 createCity("Philadelphia");
                 
                 clock.setDate(2001, 12, 14);
                 
                 createInstance(Location.class);
                 createInstance(Location.class);
                 createInstance(Location.class);
                 
                
                 Location l = (Location) createInstance(Location.class);
                 l.getKnownAs().setValue("home");
                 l.setCity(city);
                 
                 Booking b = (Booking) createInstance(Booking.class);
                 b.setPickUp(l);

                 Customer c = (Customer) createInstance(Customer.class);
                 c.getFirstName().setValue("Harry");
//                 c.addToLocations(l);
                 
                 b.associateCustomer(c);
             }
             
             private City createCity(String name) {
                 City city = (City) createInstance(City.class);
                 city.setName(name);
                 return city;
             }
         } );
     }
     
     
     public void testAllInstances() {
         TestClass cls = getTestClass(Customer.class.getName());
         TestObject customer = cls.findInstance("Harry");

         customer.invokeAction("UseAllInstances");
    }

     
     public void testAllInstancesAfterInvokingInstanceMethod1() {
         TestClass cls = getTestClass(Customer.class.getName());
         TestObject customer = cls.findInstance("Harry");
         
         customer.invokeAction("InvokeCityMethodOnLocation");
         customer.invokeAction("UseAllInstances");
    }
     
     public void testAllInstancesAfterInvokingInstanceMethod() {
         TestClass cls = getTestClass(Customer.class.getName());
         TestObject customer = cls.findInstance("Harry");
         
         customer.invokeAction("InvokeLocationMethodOnOneOfTheBookings");
         customer.invokeAction("UseAllInstances");
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
