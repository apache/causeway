package org.nakedobjects.example.ecs.fixtures;

import org.nakedobjects.example.ecs.City;
import org.nakedobjects.reflector.java.fixture.JavaFixture;

public class CitiesFixture extends JavaFixture {
    City boston;
    City newYork;
    City washington;
    
    public void install() {
        String[] cityNames = {
            "New York", "Boston", "Washington", "Chicago", "Tampa",
            "Seattle", "Atlanta"
        };

        City[] cities = new City[cityNames.length];

        for (int i = 0; i < cityNames.length; i++) {
            cities[i] = (City) createInstance(City.class);
            cities[i].setName(cityNames[i]);
        }
        
        boston = cities[1];
        newYork = cities[0];
        washington = cities[2];
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