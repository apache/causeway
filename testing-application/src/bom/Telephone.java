package bom;

import org.nakedobjects.application.BusinessObjectContainer;
import org.nakedobjects.application.Title;
import org.nakedobjects.application.control.FieldAbout;
import org.nakedobjects.application.value.IntegerNumber;
import org.nakedobjects.application.valueholder.Logical;
import org.nakedobjects.application.valueholder.TextString;


public class Telephone {
    private final TextString number;
    private final TextString knownAs;
    private final Logical temporary;
    private transient BusinessObjectContainer container;
    private IntegerNumber ringCount;
    
    public Telephone() {
        number = new TextString();
        knownAs = new TextString();
        temporary = new Logical();
    }

    public void actionConvertToOffice() {
        number.setValue(number.stringValue()+ " ext ");
        knownAs.setValue("Office");
        
        container.objectChanged(this);
    }
    
    public void aboutKnownAs(FieldAbout about, TextString entry) {
        about.unmodifiableOnCondition(temporary.isSet(), "Flag set");
    }
    
    public final TextString getKnownAs() {
        return knownAs;
    }

    public void aboutNumber(FieldAbout about, TextString entry) {
        about.unmodifiableOnCondition(temporary.isSet(), "Flag set");
    }
    

    public final TextString getNumber() {
        return number;
    }
    
    public final Logical getUnmodifiable() {
        return temporary;
    }

    public Title title() {
        if (knownAs.isEmpty()) {
            return number.title();
        } else {
            return knownAs.title();
        }
    }
    
    public IntegerNumber getRingCount() {
        return ringCount;
    }
    
    public void setRingCount(IntegerNumber ringCount) {
        this.ringCount = ringCount;
        container.objectChanged(this);
    }

    public void setContainer(BusinessObjectContainer container) {
        this.container = container;
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
