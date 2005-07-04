package bom;

import org.nakedobjects.application.BusinessObjectContainer;
import org.nakedobjects.application.Lookup;
import org.nakedobjects.application.Title;
import org.nakedobjects.application.TitledObject;
import org.nakedobjects.application.control.ClassAbout;
import org.nakedobjects.application.control.FieldAbout;
import org.nakedobjects.application.valueholder.TextString;

import java.util.Vector;


public class City implements Lookup, TitledObject {
    private TextString name = new TextString();
    private transient BusinessObjectContainer container;

    public static void aboutCity(ClassAbout about) {
        about.uninstantiable();
        about.instancesUnavailable();
    }
    
    public static Vector actionAllCities() {
        return CityRepository.instances();        
    }
    
     public void setContainer(BusinessObjectContainer container) {
        this.container = container;
    }
    
    public Location actionNewLocation() {
        Location loc = (Location) container.createInstance(Location.class);
        loc.setCity(this);
        return loc;
    }

    public void aboutName(FieldAbout about) {
    //   	about.modifiableOnlyByRole(Role.SYSADMIN);
    }

    public final TextString getName() {
        return name;
    }

    public static String pluralName() {
        return "Cities";
    }

    public String toString() {
        return name.isEmpty() ? "" : name.toString();
    }
    
    public String lookupDescription() {
        return null;
    }

    public boolean isSelected(String text) {
        return false;
    }

    public Title title() {
        if(name.isEmpty()) {
            return null;
        }
        return new Title(name);
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
