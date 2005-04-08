package org.nakedobjects.xat;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedError;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.TypedNakedCollection;
import org.nakedobjects.object.persistence.NakedObjectManager;
import org.nakedobjects.object.persistence.NotPersistableException;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.security.Session;


public class TestClassImpl extends AbstractTestObject implements TestClass {
    private NakedClass nakedClass;

    public TestClassImpl(Session session, NakedClass cls, TestObjectFactory factory) {
        super(session, factory);
         nakedClass = cls;
    }

    /**
     * Finds the instance whose title matched the one specified. A match is any
     * substring matching the specified text, and the result is the first object
     * found that gives such a match, i.e. only one object is returned even
     * though more than one match might occur.
     */
    public TestObject findInstance(String title) {
        NakedObjectSpecification type = ((NakedClass) getForNaked().getObject()).forObjectType();
        TypedNakedCollection instances = NakedObjects.getObjectManager().findInstances(type, title, true);
        if (instances.size() == 0) {
            throw new IllegalActionError("No instance found with title " + title);
        } else {
            NakedObject foundObject = instances.elementAt(0);
            return factory.createTestObject(session, foundObject);
        }
    }

    /**
     * Returns the NakedClass that this view represents.
     */
    public final Naked getForNaked() {
        return NakedObjects.getPojoAdapterFactory().createAdapter(nakedClass);
    }
  
    public String getTitle() {
        return nakedClass.getSingularName();
    }

    /**
     * Get the instances of this class.
     */
    public TestCollection instances() {
        NakedCollection instances = NakedObjects.getObjectManager().allInstances(((NakedClass) getForNaked()).forObjectType(), false);
        if (instances.size() == 0) {
            throw new IllegalActionError("Find must find at least one object");
        } else {
            return factory.createTestCollection(session, instances);
        }
    }

    /**
     * Creates a new instance of this class.
     */
    public TestObject newInstance() {
        NakedObject object = newInstance(nakedClass);

        return factory.createTestObject(session, object);
    }

    private NakedObject newInstance(NakedClass cls) {
        NakedObject object;

        NakedObjectManager objectManager = NakedObjects.getObjectManager();
        try {
            object = objectManager.createTransientInstance(cls.forObjectType());
            objectManager.makePersistent(object);
            object.created();
            objectManager.saveChanges();
        } catch (NotPersistableException e) {
            NakedError error = objectManager.generatorError(
                    "Failed to create instance of " + cls.forObjectType().getFullName(), e);
            object = NakedObjects.getPojoAdapterFactory().createNOAdapter(error);

            System.out.println("Failed to create instance of " + cls.forObjectType().getFullName());
            e.printStackTrace();
        }

        return object;
    }

    public void setForNaked(Naked object) {
        throw new NakedObjectRuntimeException();
    }
    
    protected Action getAction(String name, NakedObjectSpecification[] parameterClasses) {
        NakedObjectSpecification spec = this.nakedClass.forObjectType();
        return spec.getClassAction(Action.USER, simpleName(name), parameterClasses);
        
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