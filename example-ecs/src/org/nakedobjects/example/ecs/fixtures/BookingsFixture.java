package org.nakedobjects.example.ecs.fixtures;

import org.nakedobjects.example.ecs.Booking;
import org.nakedobjects.example.ecs.City;
import org.nakedobjects.example.ecs.CreditCard;
import org.nakedobjects.example.ecs.Customer;
import org.nakedobjects.example.ecs.Location;
import org.nakedobjects.example.ecs.Telephone;
import org.nakedobjects.reflector.java.fixture.JavaFixture;

public class BookingsFixture extends JavaFixture {
    
    private final CitiesFixture cities;

    public BookingsFixture(CitiesFixture cities) {
        this.cities = cities;}

    public void install() {
        setDate(2003, 10, 23);
        setTime(20, 15);

        Customer newCustomer = (Customer) createInstance(Customer.class);
        newCustomer.getFirstName().setValue("Richard");
        newCustomer.getLastName().setValue("Pawson");

        City boston = cities.boston;
        City newYork = cities.newYork;
        City washington = cities.washington;
        
        Location l = (Location) createInstance(Location.class);
        l.setCity(boston);
        l.getKnownAs().setValue("Home");
        l.getStreetAddress().setValue("433 Pine St.");
        newCustomer.addToLocations(l);

        l = (Location) createInstance(Location.class);
        l.setCity(boston);
        l.getKnownAs().setValue("Office");
        l.getStreetAddress().setValue("944 Main St, Cambridge");
        newCustomer.addToLocations(l);

        l = (Location) createInstance(Location.class);
        l.setCity(newYork);
        l.getKnownAs().setValue("QIC Headquaters");
        l.getStreetAddress().setValue("285 Park Avenue");
        newCustomer.addToLocations(l);

        l = (Location) createInstance(Location.class);
        l.setCity(newYork);
//        l.getKnownAs().setValue("PPO Headquaters");
        l.getStreetAddress().setValue("234 E 42nd Street");
        newCustomer.addToLocations(l);

        l = (Location) createInstance(Location.class);
        l.setCity(newYork);
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
  //          booking.getTime().setValue(14, 50);

        l = (Location) createInstance(Location.class);
        l.setCity(washington);
        l.getKnownAs().setValue("Home");
        l.getStreetAddress().setValue("1112 Condor St, Carlton Park");
        newCustomer.addToLocations(l);
        booking.setPickUp(l);

        l = (Location) createInstance(Location.class);
        l.setCity(washington);
        l.getKnownAs().setValue("Office");
        l.getStreetAddress().setValue("299 Union St");
        newCustomer.addToLocations(l);

        l = (Location) createInstance(Location.class);
        l.setCity(newYork);
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
        

        resetClock();
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