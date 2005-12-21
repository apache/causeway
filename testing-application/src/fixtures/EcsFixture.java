package fixtures;

import org.nakedobjects.reflector.java.fixture.JavaFixture;

import bom.Booking;
import bom.City;
import bom.CreditCard;
import bom.Customer;
import bom.Location;
import bom.Telephone;


public class EcsFixture extends JavaFixture {

    public void install() {
        // setupClasses();
        setupClock();
        City[] cities = setupCities();
        setupObjects(cities);
        // setUsers();
        resetClock();
    }

    /*
     * private void setUsers() { User joe = addUser("Joe"); addRole("entry", joe); Role delete =
     * addRole("delete", joe);
     * 
     * User admin = addUser("Admin"); addRole(delete, admin); addRole("admin", admin);
     * 
     * User sysadmin = addUser("sysadmin"); addRole("sysadmin", sysadmin); }
     */

    private void setupObjects(City[] cities) {
        Customer newCustomer = (Customer) createInstance(Customer.class);
        newCustomer.getFirstName().setValue("Richard");
        newCustomer.getLastName().setValue("Pawson");

        Location l = (Location) createInstance(Location.class);
        l.setCity(cities[1]);
        l.getKnownAs().setValue("Home");
        l.getStreetAddress().setValue("433 Pine St.");
        newCustomer.addToLocations(l);

        l = (Location) createInstance(Location.class);
        l.setCity(cities[1]);
        l.getKnownAs().setValue("Office");
        l.getStreetAddress().setValue("944 Main St, Cambridge");
        newCustomer.addToLocations(l);

        l = (Location) createInstance(Location.class);
        l.setCity(cities[0]);
        l.getKnownAs().setValue("QIC Headquaters");
        l.getStreetAddress().setValue("285 Park Avenue");
        newCustomer.addToLocations(l);

        l = (Location) createInstance(Location.class);
        l.setCity(cities[0]);
        // l.getKnownAs().setValue("PPO Headquaters");
        l.getStreetAddress().setValue("234 E 42nd Street");
        newCustomer.addToLocations(l);

        l = (Location) createInstance(Location.class);
        l.setCity(cities[0]);
        l.getKnownAs().setValue("JFK Airport, BA Terminal");
        newCustomer.addToLocations(l);

        Telephone t = (Telephone) createInstance(Telephone.class);
        t.getKnownAs().setValue("Home");
        t.getNumber().setValue("617/211 2899");
        newCustomer.getPhoneNumbers().addElement(t);

        t = (Telephone) createInstance(Telephone.class);
        t.getKnownAs().setValue("Office");
        t.getNumber().setValue("617/353 9828");
        newCustomer.getPhoneNumbers().addElement(t);

        t = (Telephone) createInstance(Telephone.class);
        t.getKnownAs().setValue("Mobile");
        t.getNumber().setValue("8777662671");
        newCustomer.getPhoneNumbers().addElement(t);

        CreditCard cc = (CreditCard) createInstance(CreditCard.class);
        cc.getNumber().setValue("4525365234232233");
        cc.getExpires().setValue("12/06");
        cc.getNameOnCard().setValue("MR R Pawson");
        newCustomer.setPreferredPaymentMethod(cc);

        newCustomer = (Customer) createInstance(Customer.class);
        newCustomer.getFirstName().setValue("Robert");
        newCustomer.getLastName().setValue("Matthews");

        Booking booking = (Booking) createInstance(Booking.class);
        booking.associateCustomer(newCustomer);
        // booking.getTime().setValue(14, 50);

        l = (Location) createInstance(Location.class);
        l.setCity(cities[5]);
        l.getKnownAs().setValue("Home");
        l.getStreetAddress().setValue("1112 Condor St, Carlton Park");
        newCustomer.addToLocations(l);
        booking.setPickUp(l);

        l = (Location) createInstance(Location.class);
        l.setCity(cities[5]);
        l.getKnownAs().setValue("Office");
        l.getStreetAddress().setValue("299 Union St");
        newCustomer.addToLocations(l);

        l = (Location) createInstance(Location.class);
        l.setCity(cities[0]);
        l.getKnownAs().setValue("Headquaters");
        l.getStreetAddress().setValue("285 Park Avenue");
        newCustomer.addToLocations(l);
        booking.setDropOff(l);

        t = (Telephone) createInstance(Telephone.class);
        t.getKnownAs().setValue("Home");
        t.getNumber().setValue("206/545 8444");
        newCustomer.getPhoneNumbers().addElement(t);
        booking.setContactTelephone(t);

        t = (Telephone) createInstance(Telephone.class);
        t.getKnownAs().setValue("Office");
        t.getNumber().setValue("206/234 443");
        newCustomer.getPhoneNumbers().addElement(t);

        cc = (CreditCard) createInstance(CreditCard.class);
        cc.getNumber().setValue("773829889938221");
        cc.getExpires().setValue("10/04");
        cc.getNameOnCard().setValue("MR R MATTHEWS");
        booking.setPaymentMethod(cc);
    }

    private City[] setupCities() {
        String[] cityNames = { "New York", "Boston", "Washington", "Chicago", "Tampa", "Seattle", "Atlanta" };

        City[] cities = new City[cityNames.length];

        for (int i = 0; i < cityNames.length; i++) {
            cities[i] = (City) createInstance(City.class);
            cities[i].getName().setValue(cityNames[i]);
        }
        return cities;
    }

    private void setupClock() {
        setDate(2003, 10, 23);
        setTime(20, 15);
    }

    private void setupClasses() {
        registerClass(Booking.class);
        registerClass(City.class);
        registerClass(Location.class);
        registerClass(CreditCard.class);
        registerClass(Customer.class);
        registerClass(Telephone.class);
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */