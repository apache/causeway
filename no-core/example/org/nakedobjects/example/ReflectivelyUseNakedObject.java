package org.nakedobjects.example;

import org.nakedobjects.container.configuration.ComponentException;
import org.nakedobjects.container.configuration.ConfigurationException;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.defaults.LocalObjectManager;
import org.nakedobjects.object.defaults.LocalReflectionFactory;
import org.nakedobjects.object.defaults.NakedObjectSpecificationImpl;
import org.nakedobjects.object.defaults.NullUpdateNotifier;
import org.nakedobjects.object.defaults.TransientObjectStore;
import org.nakedobjects.object.reflect.FieldSpecification;
import org.nakedobjects.object.reflect.OneToManyAssociationSpecification;
import org.nakedobjects.object.reflect.OneToOneAssociationSpecification;
import org.nakedobjects.system.SystemClock;


public class ReflectivelyUseNakedObject {

    public static void main(String[] args) throws ConfigurationException, ComponentException {
        new SystemClock();
        NakedObjectSpecificationImpl.setReflectionFactory(new LocalReflectionFactory());
        new LocalObjectManager(new TransientObjectStore(), new NullUpdateNotifier(), null);
        
        NakedObject object = setupObject();
        useReflection(object);
    }
    
    private static NakedObject setupObject() {
        Customer customer = new Customer();
        customer.getName().setValue("John H Smith");
        
        Transaction t1 = new Transaction();
        t1.getDate().setValue(2004, 10, 20);
        Transaction t2 = new Transaction();
        t2.getDate().setValue(2004, 10, 23);
        Transaction t3 = new Transaction();
        t3.getDate().setValue(2004, 10, 28);
        
        Account account = new Account();
        account.getDateOpened().setValue(2004, 10, 20);
        account.getAccount().setValue("Savings");
        account.setCustomer(customer);
        account.getTransactions().add(t1);
        account.getTransactions().add(t2);
        account.getTransactions().add(t3);
        
        return account;
    }

    private static void useReflection(NakedObject object) {        
        NakedObjectSpecification specification = object.getSpecification();
        FieldSpecification[] fields = specification.getFields();

        for (int i = 0; i < fields.length; i++) {
            FieldSpecification field = fields[i];
            String label = field.getLabel();
            System.out.print(label + ": ");

            if (field instanceof OneToManyAssociationSpecification) {
                NakedCollection collection = (NakedCollection) field.get(object);
                for (int j = 0, len = collection.size(); j < len; j++) {
                    System.out.println("    " + collection.elementAt(j).titleString());                    
                }

            } else if (field instanceof OneToOneAssociationSpecification) {
                NakedObject association = (NakedObject) field.get(object);
                System.out.println(association.titleString());

            } else if (field instanceof FieldSpecification) {
                NakedValue value = (NakedValue) field.get(object);
                System.out.println(value.titleString());
            }
        }

    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
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