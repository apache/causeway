package bom;

import org.nakedobjects.application.BusinessObjectContainer;
import org.nakedobjects.application.valueholder.TextString;

import java.util.Vector;

public class TestObject {
    private BusinessObjectContainer container;
    private TextString name = new TextString("New");
    private Location location;
    private Vector objects = new Vector();

    public void actionCreateObject() {
        Location location = createLocation("Default");
        setLocation(location);
        
        getName().setValue("Action run");
    }

    public void actionCreateObjects() {
        Location location = createLocation("Main location");
        setLocation(location);

        for (int i = 0; i < 8; i++) {
            location = createLocation("Location " + i);
            addToObjects(location);    
        }
        getName().setValue("Other action run");
    }

    private Location createLocation(String knownas) {
        Location location = (Location) container.createTransientInstance(Location.class);
        Vector allInstances = container.allInstances(City.class);
        location.setCity((City) allInstances.elementAt((int) (Math.random() * allInstances.size())));
        Vector allInstances2 = container.allInstances(Customer.class);
        location.setCustomer((Customer) allInstances2.elementAt((int) (Math.random() * allInstances2.size())));
        location.getKnownAs().setValue(knownas);
        location.getStreetAddress().setValue("A street name");
        container.makePersistent(location);
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
        container.objectChanged(this);
    }

    public Location getLocation() {
        container.resolve(this, location);
        return location;
    }
    
    public Vector getObjects() {
        return objects ;
    }
    
    public void addToObjects(Location location) {
        objects.addElement(location);
        container.objectChanged(this);
    }
    
    public void removeFromObjects(Location location) {
        objects.removeElement(location);
        container.objectChanged(this);
    }

    public TextString getName() {
        return name ;
    }

    public String title() {
        return name.stringValue();
    }

    public void setContainer(BusinessObjectContainer container) {
        this.container = container;
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