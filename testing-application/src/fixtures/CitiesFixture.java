package fixtures;

import org.nakedobjects.reflector.java.fixture.JavaFixture;

import bom.City;


public class CitiesFixture extends JavaFixture {
    City boston;
    City newYork;
    City washington;

    public void install() {
        String[] cityNames = { "Atlanta", "Austin", "Birmingham", "Boston", "Charleston", "Chicago", "Cleveland", "Concord",
                "Dallas", "Denver", "Des Moines", "Fort Worth", "Las Vegas", "Manchester", "Miami", "Newark", "New York",
                "Paris", "Portsmouth", "Providence", "Pittsburg", "Richmond", "Sacremento", "San Fransico", "Seattle", "Tampa",
                "Washington", };

        City[] cities = new City[cityNames.length];

        for (int i = 0; i < cityNames.length; i++) {
            cities[i] = (City) createInstance(City.class);
            cities[i].getName().setValue(cityNames[i]);
        }

        boston = cities[3];
        newYork = cities[16];
        washington = cities[26];
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
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