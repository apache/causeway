package org.nakedobjects.object.testobject;


import org.nakedobjects.application.Title;
import org.nakedobjects.application.control.ActionAbout;
import org.nakedobjects.application.control.FieldAbout;
import org.nakedobjects.application.valueholder.Logical;
import org.nakedobjects.application.valueholder.Money;
import org.nakedobjects.application.valueholder.TextString;
import org.nakedobjects.object.NakedObject;


public class ContactTestObject {
    private static final long serialVersionUID = 1L;
    private final TextString name;
    private final TextString address;
    private final Money worth;
    private final Logical isContact;
    private GenericTestObject favourite;

    public ContactTestObject() {
        name = new TextString();
        address = new TextString();
        worth = new Money();
        isContact = new Logical(true);
    }

    /**
     invalid - no About return
     */
    public void aboutActionCreateInvoice() {}

    public void aboutActionDuplicate(ActionAbout about) {
    	about.unusable();
    }

    /**
     invalid - non matching
     */
    public void aboutActionInvalid(ActionAbout about) {
    }

    public void aboutActionResetWorth(ActionAbout about) {
    	about.unusable();
    }

    public void aboutActionSetUp(ActionAbout about) {
    }

    public void aboutIsContact(FieldAbout about) {
    }

    public void aboutName(FieldAbout about) {
    	about.unusable();
    }

    public void aboutActionAddContact(ActionAbout about, ContactTestObject o) {
    }

    public void aboutWorth(FieldAbout about) {
        about.unusable();
    }

    public NakedObject actionCreateInvoice() {
        return null;
    }

    public void actionResetWorth() {}

    public void actionSetUp() {
        getAddress().setValue("1 High Street");
        getName().setValue("Fred Smith");
    }

    public void addFavourite(GenericTestObject newFavourite) {

        /*
         If reassigning (which should not happen through the viewers) then clear the existing
         */
        if (favourite != null) {
            favourite.setCustomer(null);
        }
        setFavourite(newFavourite);
        if (newFavourite != null) {
            newFavourite.setCustomer(this);
        }
    }

    public static void actionClassOp() {}

    public static void aboutActionClassOp(ActionAbout about) {
    }

    public static void actionDoWhatever() {}

    public static String pluralName() {
        return "Contacts";
    }
    public static String singularName() {
        return "Contact";
    }
    
    public void classActionInvalidClassOp() {}

    public static void aboutActionExample(ActionAbout about, GenericTestObject o) {
    }

    public static void actionExample(GenericTestObject o) {}

    public static String fieldOrder() {
        return "Name, Worth, Is Contact, Address";
    }

    public static String actionOrder() {
        return "Do Whatever, Create Invoice, Duplicate";
    }

    public static String classActionOrder() {
        return "Class Op, Do Whatever";
    }

    public TextString getAddress() {
        return address;
    }

    public GenericTestObject getFavourite() {
        return favourite;
    }

    /**
     Invalid attribute - not public
     */
    TextString getInvalidAttibute1() {
        return address;
    }

    /**
     Invalid attribute - no Return type
     */
    public void getInvalidAttibute2() {
        return;
    }

    /**
     Invalid attribute - non Naked return type
     */
    public void getInvalidAttibute3() {
        return;
    }

    public Logical getIsContact() {
        return isContact;
    }

    public TextString getName() {
        return name;
    }

    public Money getWorth() {
        return worth;
    }

    public void init() {}

    public void actionAddContact(ContactTestObject o) {}

    public ProductTestObject actionRenew(ProductTestObject o) {
        return null;
    }

    public void removeFavourite(GenericTestObject newFavourite) {
        newFavourite.setCustomer(null);
        setFavourite(null);
    }

    public void setFavourite(GenericTestObject newFavourite) {
        favourite = newFavourite;
    }

    public Title title() {
        return getName().title();
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

