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

package org.nakedobjects.object.testobject;


import org.nakedobjects.application.Title;
import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.NakedObject;


public class GenericTestObject {
    private static final long serialVersionUID = 1L;

    public static void actionAcceptContact(ContactTestObject test) {}

    public static String attributeOrder() {
        return "Wrong One, Customer, Products, Another";
    }

    public static String fieldOrder() {
        return "Wrong One, Customer, Products, Another";
    }

    public static String pluralName() {
        return "Generic Objects";
    }
    private ContactTestObject customer;
    private final InternalCollection products = createInternalCollection(ProductTestObject.class);

    public NakedObject actionTest() {
        return customer;
    }

    public void addCustomer(ContactTestObject newCustomer) {
        if (customer != null) {
            customer.setFavourite(null);
        }
        setCustomer(newCustomer);
        if (newCustomer != null) {
            newCustomer.setFavourite(this);
        }
    }

    public void addProducts(ProductTestObject product) {
        products.add(product);
    }

    public ContactTestObject getCustomer() {
        return customer;
    }

    public InternalCollection getProducts() {
        return products;
    }

    public void removeCustomer(ContactTestObject customer) {
        customer.setFavourite(null);
        setCustomer(null);
    }

    public void setCustomer(ContactTestObject newCustomer) {
        customer = newCustomer;
    }

    public Title title() {
        return customer == null ? new Title("NONE") : customer.title();
    }
}
