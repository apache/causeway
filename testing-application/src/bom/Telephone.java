package bom;

import org.nakedobjects.application.BusinessObjectContainer;
import org.nakedobjects.application.Title;
import org.nakedobjects.application.control.ActionAbout;
import org.nakedobjects.application.control.FieldAbout;
import org.nakedobjects.application.valueholder.Logical;
import org.nakedobjects.application.valueholder.TextString;


public class Telephone implements Common {
    private final TextString number;
    private final TextString knownAs;
    private final Logical temporary;
    private final Logical hide;
    private transient BusinessObjectContainer container;
    
    public Telephone() {
        number = new TextString();
        knownAs = new TextString();
        temporary = new Logical();
        hide = new Logical();
    }

    public void aboutActionConvertToOffice(ActionAbout about) {
        if(temporary.isSet()) about.unusable();
        if(hide.isSet()) about.invisible();
    }

    public void actionConvertToOffice() {
        number.setValue(number.stringValue()+ " ext ");
        knownAs.setValue("Office");
        
        container.objectChanged(this);
    }

    public void actionLocalConvertToOfficeOnClient() {
        number.setValue(number.stringValue()+ " ext ");
        knownAs.setValue("Office");
        
        container.objectChanged(this);
    }
    
    public void actionRemoteTestSaveInActionOnServer() {
        container.makePersistent(this);
    }
    
    public Telephone actionCreateTransient() {
        Telephone copy = (Telephone) container.createTransientInstance(Telephone.class);
        copy.number.setValue(number);
        copy.knownAs.setValue(knownAs);
        return copy;
    }
    
    public void aboutKnownAs(FieldAbout about, TextString entry) {
        about.unmodifiableOnCondition(temporary.isSet(), "Flag set");
        if(hide.isSet()) about.invisible();
    }
    
    public final TextString getKnownAs() {
        return knownAs;
    }

    public final TextString getHiddenExtra() {
        return knownAs;
    }

    public void aboutNumber(FieldAbout about, TextString entry) {
        about.unmodifiableOnCondition(temporary.isSet(), "Flag set");
        if(hide.isSet()) about.invisible();
    }
    
    public void aboutHideFields(FieldAbout about, Logical entry) {
        about.unmodifiableOnCondition(temporary.isSet(), "Flag set");
      }

    public final TextString getNumber() {
        return number;
    }
    
    public final String getReversed() {
        StringBuffer n = new StringBuffer(number.titleString());
        return n.reverse().toString();
    }
    
    public final Logical getUnmodifiable() {
        return temporary;
    }

    public final Logical getHideFields() {
        return hide;
    }

    public Title title() {
        if (knownAs.isEmpty()) {
            return number.title();
        } else {
            return knownAs.title();
        }
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
