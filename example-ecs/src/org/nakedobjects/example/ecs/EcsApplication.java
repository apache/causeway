package org.nakedobjects.example.ecs;

import org.nakedobjects.application.valueholder.TextString;
import org.nakedobjects.object.NakedClass;

import java.util.Vector;


public class EcsApplication {
    Vector classes = new Vector();
    private TextString name = new TextString();
    
    public EcsApplication() {
        classes.addElement(new NakedClass(City.class.getName()));
        classes.addElement(new NakedClass(Telephone.class.getName()));
        classes.addElement(new NakedClass(CreditCard.class.getName()));
        
        classes.addElement(new NakedClass(Location.class.getName()));
        classes.addElement(new NakedClass(Booking.class.getName()));
        classes.addElement(new NakedClass(Customer.class.getName()));
        
        classes.addElement(new NakedClass(CompanyAccount.class.getName()));
      
    }
    
    public void addToClasses(NakedClass cls) {
        
    }
    
    public void removeFromClasses(NakedClass cls) {
        
    }
    
     public Vector getClasses() {
        return classes;
    }
     
     public TextString getName() {
         return name;
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